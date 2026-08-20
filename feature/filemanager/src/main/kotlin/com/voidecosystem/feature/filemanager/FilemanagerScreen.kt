package com.voidecosystem.feature.filemanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.voidecosystem.core.ui.EmptyState

object FilemanagerDestination {
    const val ROUTE = "filemanager"
}

private const val PREFS_NAME = "filemanager_prefs"
private const val KEY_TREE_URI = "tree_uri"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilemanagerRoute(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var rootUri by remember { mutableStateOf(prefs.getString(KEY_TREE_URI, null)?.toUri()) }
    var pathStack by rememberSaveable(stateSaver = uriListSaver) { mutableStateOf(emptyList<Uri>()) }

    val pickFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            prefs.edit().putString(KEY_TREE_URI, uri.toString()).apply()
            rootUri = uri
            pathStack = emptyList()
        }
    }

    val currentDir: DocumentFile? = remember(rootUri, pathStack) {
        val root = rootUri?.let { DocumentFile.fromTreeUri(context, it) } ?: return@remember null
        pathStack.fold(root) { dir, uri ->
            dir?.listFiles()?.firstOrNull { it.uri == uri } ?: dir
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentDir?.name ?: "File Manager") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pathStack.isNotEmpty()) {
                            pathStack = pathStack.dropLast(1)
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { pickFolderLauncher.launch(null) }) {
                        Icon(Icons.Filled.CreateNewFolder, contentDescription = "Change folder")
                    }
                },
            )
        },
    ) { padding ->
        if (rootUri == null || currentDir == null) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No folder selected", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Android requires you to pick which folder to browse — apps can't see arbitrary storage without it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = { pickFolderLauncher.launch(null) }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Choose folder")
                }
            }
        } else {
            val entries = remember(currentDir) {
                currentDir.listFiles()
                    .sortedWith(compareByDescending<DocumentFile> { it.isDirectory }.thenBy { it.name ?: "" })
            }
            if (entries.isEmpty()) {
                EmptyState(title = "Empty folder", message = "Nothing here.", modifier = Modifier.padding(padding))
            } else {
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    items(entries, key = { it.uri.toString() }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (entry.isDirectory) {
                                        pathStack = pathStack + entry.uri
                                    } else {
                                        openFile(context, entry)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Icon(
                                imageVector = if (entry.isDirectory) Icons.Filled.Folder else Icons.Filled.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = entry.name ?: "Unnamed",
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (!entry.isDirectory) {
                                    Text(
                                        text = formatBytes(entry.length()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openFile(context: Context, file: DocumentFile) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(file.uri, file.type ?: "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}

private val uriListSaver: Saver<List<Uri>, List<String>> = listSaver(
    save = { list -> list.map { it.toString() } },
    restore = { list -> list.map { it.toUri() } },
)
