package com.voidecosystem.feature.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CalendarDatabase.getInstance(application).eventDao()

    val events: StateFlow<List<CalendarEvent>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEvent(title: String, dateMillis: Long, notes: String) {
        if (title.isBlank()) return
        viewModelScope.launch { dao.upsert(CalendarEvent(title = title.trim(), dateMillis = dateMillis, notes = notes.trim())) }
    }

    fun delete(event: CalendarEvent) {
        viewModelScope.launch { dao.deleteById(event.id) }
    }
}
