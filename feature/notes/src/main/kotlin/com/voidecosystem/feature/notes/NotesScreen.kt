package com.voidecosystem.feature.notes

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
                onSave = { title, body ->
                    viewModel.save(editingNote?.id, title, body) {
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
    onSave: (title: String, body: String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember { mutableStateOf(note?.title.orEmpty()) }
    var body by remember { mutableStateOf(note?.body.orEmpty()) }

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
                    IconButton(onClick = { onSave(title, body) }) {
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
            TextField(
                value = body,
                onValueChange = { body = it },
                placeholder = { Text("Start writing…") },
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            )
        }
    }
}
