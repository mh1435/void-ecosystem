package com.voidecosystem.feature.notes

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voidecosystem.core.ui.EmptyState

object NotesDestination {
    const val ROUTE = "notes"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesRoute(
    onBack: () -> Unit = {},
    viewModel: NotesViewModel = viewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    when {
        isCreatingNew || editingNote != null -> {
            NoteEditor(
                note = editingNote,
                onBack = { isCreatingNew = false; editingNote = null },
                onSave = { title, body, audioFilePath ->
                    viewModel.save(editingNote?.id, title, body, audioFilePath) {
                        isCreatingNew = false
                        editingNote = null
                    }
                },
                onDelete = editingNote?.let { note ->
                    {
                        viewModel.delete(note.id)
                        editingNote = null
                    }
                },
            )
        }
        else -> {
            NotesList(
                notes = notes,
                onBack = onBack,
                onNoteClick = { editingNote = it },
                onNewNote = { isCreatingNew = true },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesList(
    notes: List<Note>,
    onBack: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onNewNote: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) {
                Icon(Icons.Filled.Add, contentDescription = "New note")
            }
        },
    ) { padding ->
        if (notes.isEmpty()) {
            EmptyState(
                title = "No notes yet",
                message = "Tap + to write your first note.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp)) {
                items(notes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        onClick = { onNoteClick(note) },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                note.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
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
private fun NoteEditor(
    note: Note?,
    onBack: () -> Unit,
    onSave: (title: String, body: String, audioFilePath: String?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(note?.title.orEmpty()) }
    var body by remember { mutableStateOf(note?.body.orEmpty()) }
    var audioFilePath by remember { mutableStateOf(note?.audioFilePath) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val recorder = remember { NoteAudioRecorder(context) }
    val player = remember { NoteAudioPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) recorder.cancel()
            player.stop()
        }
    }

    fun startRecording() {
        audioFilePath = null
        isRecording = true
        recorder.start()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasMicPermission = granted
        if (granted) startRecording()
    }

    fun stopRecording() {
        isRecording = false
        audioFilePath = recorder.stop()?.absolutePath
    }

    fun togglePlayback() {
        val path = audioFilePath ?: return
        if (isPlaying) {
            player.stop()
            isPlaying = false
        } else {
            isPlaying = true
            player.play(path) { isPlaying = false }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "New note" else "Edit note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete note")
                        }
                    }
                    IconButton(onClick = { onSave(title, body, audioFilePath) }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save note")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title") },
                textStyle = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = {
                        when {
                            isRecording -> stopRecording()
                            !hasMicPermission -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            else -> startRecording()
                        }
                    },
                ) {
                    Icon(
                        if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (isRecording) "Stop recording" else "Record voice note",
                    )
                }
                Text(
                    text = when {
                        isRecording -> "Recording…"
                        audioFilePath != null -> "Voice note attached"
                        else -> "Attach a voice note"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp),
                )
                if (audioFilePath != null && !isRecording) {
                    IconButton(onClick = ::togglePlayback) {
                        Icon(
                            if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Stop playback" else "Play voice note",
                        )
                    }
                    IconButton(onClick = {
                        player.stop()
                        isPlaying = false
                        val path = audioFilePath
                        audioFilePath = null
                        if (path != null && path != note?.audioFilePath) java.io.File(path).delete()
                    }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove voice note")
                    }
                }
            }

            TextField(
                value = body,
                onValueChange = { body = it },
                placeholder = { Text("Start writing…") },
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            )
        }
    }
}
