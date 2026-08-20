package com.voidecosystem.feature.terminal

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class TerminalEntry(val command: String, val output: String, val exitCode: Int)

/**
 * Runs shell commands as this app's own process — no root, no special
 * permission, and nothing beyond what any Android app can already do.
 * The working directory is the app's private `filesDir`, so scripts read
 * and write files there rather than anywhere else on the device — a real
 * sandbox, not a simulated one.
 */
class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val workingDir: File = application.filesDir

    var history by mutableStateOf<List<TerminalEntry>>(emptyList())
        private set

    var isRunning by mutableStateOf(false)
        private set

    fun execute(rawCommand: String) {
        val command = rawCommand.trim()
        if (command.isEmpty() || isRunning) return

        if (command == "clear") {
            history = emptyList()
            return
        }

        isRunning = true
        viewModelScope.launch {
            val entry = withContext(Dispatchers.IO) { runCommand(command) }
            history = history + entry
            isRunning = false
        }
    }

    private fun runCommand(command: String): TerminalEntry = try {
        val process = ProcessBuilder("sh", "-c", command)
            .redirectErrorStream(true)
            .directory(workingDir)
            .start()
        val output = process.inputStream.bufferedReader().readText().trimEnd('\n')
        val exitCode = process.waitFor()
        TerminalEntry(command, output, exitCode)
    } catch (e: Exception) {
        TerminalEntry(command, "error: ${e.message}", -1)
    }
}
