package com.voidecosystem.feature.dialer

import android.app.Application
import android.provider.ContactsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ContactEntry(val id: String, val name: String, val number: String)

class DialerViewModel(application: Application) : AndroidViewModel(application) {

    var contacts by mutableStateOf<List<ContactEntry>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadContacts() {
        isLoading = true
        viewModelScope.launch {
            contacts = withContext(Dispatchers.IO) { queryContacts() }
            isLoading = false
        }
    }

    private fun queryContacts(): List<ContactEntry> {
        val context = getApplication<Application>()
        val result = mutableListOf<ContactEntry>()
        val seen = HashSet<String>()

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex) ?: continue
                val number = cursor.getString(numberIndex) ?: continue
                val key = "$name|$number"
                if (seen.add(key)) {
                    result.add(ContactEntry(cursor.getString(idIndex), name, number))
                }
            }
        }
        return result
    }
}
