package com.voidecosystem.feature.pantry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PantryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PantryDatabase.getInstance(application).pantryDao()

    val items: StateFlow<List<PantryItem>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(name: String, rating: Int, flavorNotes: String, dietaryTags: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.upsert(
                PantryItem(
                    name = name.trim(),
                    rating = rating.coerceIn(1, 5),
                    flavorNotes = flavorNotes.trim(),
                    dietaryTags = dietaryTags.trim(),
                ),
            )
        }
    }

    fun delete(item: PantryItem) {
        viewModelScope.launch { dao.deleteById(item.id) }
    }
}
