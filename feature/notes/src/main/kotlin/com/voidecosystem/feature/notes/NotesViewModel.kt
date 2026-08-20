package com.voidecosystem.feature.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NoteDatabase.getInstance(application).noteDao()

    val notes: StateFlow<List<Note>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(id: Long?, title: String, body: String, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = dao.upsert(
                Note(
                    id = id ?: 0,
                    title = title.ifBlank { "Untitled" },
                    body = body,
                ),
            )
            onSaved(if (id != null && id != 0L) id else newId)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { dao.deleteById(id) }
    }
}
