package com.voidecosystem.feature.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NoteDatabase.getInstance(application).noteDao()

    val notes: StateFlow<List<Note>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(id: Long?, title: String, body: String, audioFilePath: String?, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val previousAudioPath = if (id != null && id != 0L) dao.getById(id)?.audioFilePath else null
            if (previousAudioPath != null && previousAudioPath != audioFilePath) {
                withContext(Dispatchers.IO) { File(previousAudioPath).delete() }
            }
            val newId = dao.upsert(
                Note(
                    id = id ?: 0,
                    title = title.ifBlank { "Untitled" },
                    body = body,
                    audioFilePath = audioFilePath,
                ),
            )
            onSaved(if (id != null && id != 0L) id else newId)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            val audioPath = dao.getById(id)?.audioFilePath
            dao.deleteById(id)
            if (audioPath != null) {
                withContext(Dispatchers.IO) { File(audioPath).delete() }
            }
        }
    }
}
