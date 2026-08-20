package com.voidecosystem.feature.journal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voidecosystem.core.ui.EmptyState
import java.text.DateFormat
import java.util.Date

object JournalDestination {
    const val ROUTE = "journal"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalRoute(
    onBack: () -> Unit = {},
    viewModel: JournalViewModel = viewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var editingEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    when {
        isCreatingNew -> {
            JournalEditor(
                initialBody = "",
                dateLabel = "Today",
                onBack = { isCreatingNew = false },
                onSave = { body -> viewModel.addEntry(body); isCreatingNew = false },
                onDelete = null,
            )
        }
        editingEntry != null -> {
            val entry = editingEntry!!
            JournalEditor(
                initialBody = entry.body,
                dateLabel = formatDate(entry.createdAt),
                onBack = { editingEntry = null },
                onSave = { body -> viewModel.updateEntry(entry, body); editingEntry = null },
                onDelete = { viewModel.delete(entry); editingEntry = null },
            )
        }
        else -> {
            JournalList(
                entries = entries,
                onBack = onBack,
                onEntryClick = { editingEntry = it },
                onNewEntry = { isCreatingNew = true },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalList(
    entries: List<JournalEntry>,
    onBack: () -> Unit,
    onEntryClick: (JournalEntry) -> Unit,
    onNewEntry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewEntry) {
                Icon(Icons.Filled.Add, contentDescription = "New entry")
            }
        },
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState(
                title = "No entries yet",
                message = "Tap + to write your first journal entry.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp)) {
                items(entries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        onClick = { onEntryClick(entry) },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                formatDate(entry.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                entry.body,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalEditor(
    initialBody: String,
    dateLabel: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var body by remember { mutableStateOf(initialBody) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dateLabel) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete entry")
                        }
                    }
                    IconButton(onClick = { onSave(body) }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save entry")
                    }
                },
            )
        },
    ) { padding ->
        TextField(
            value = body,
            onValueChange = { body = it },
            placeholder = { Text("What's on your mind today?") },
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
        )
    }
}

private fun formatDate(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(millis))
