package com.voidecosystem.feature.finance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = FinanceDatabase.getInstance(application).transactionDao()

    val transactions: StateFlow<List<Transaction>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<Double> = dao.observeAll()
        .map { list -> list.sumOf { if (it.isExpense) -it.amount else it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(amount: Double, category: String, note: String, isExpense: Boolean) {
        if (amount <= 0.0 || category.isBlank()) return
        viewModelScope.launch {
            dao.upsert(Transaction(amount = amount, category = category.trim(), note = note.trim(), isExpense = isExpense))
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { dao.deleteById(transaction.id) }
    }
}
