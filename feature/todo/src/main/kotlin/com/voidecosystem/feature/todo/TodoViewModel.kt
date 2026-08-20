package com.voidecosystem.feature.todo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = TodoDatabase.getInstance(application).todoDao()

    val items: StateFlow<List<TodoItem>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTodo(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { dao.upsert(TodoItem(title = title.trim())) }
    }

    fun toggleDone(item: TodoItem) {
        viewModelScope.launch { dao.update(item.copy(isDone = !item.isDone)) }
    }

    fun delete(item: TodoItem) {
        viewModelScope.launch { dao.deleteById(item.id) }
    }
}
