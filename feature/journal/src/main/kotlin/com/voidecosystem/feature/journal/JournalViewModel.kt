package com.voidecosystem.feature.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = JournalDatabase.getInstance(application).journalDao()

    val entries: StateFlow<List<JournalEntry>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(body: String) {
        if (body.isBlank()) return
        viewModelScope.launch { dao.upsert(JournalEntry(body = body.trim())) }
    }

    fun updateEntry(entry: JournalEntry, newBody: String) {
        viewModelScope.launch { dao.upsert(entry.copy(body = newBody, updatedAt = System.currentTimeMillis())) }
    }

    fun delete(entry: JournalEntry) {
        viewModelScope.launch { dao.deleteById(entry.id) }
    }
}
