package com.voidecosystem.feature.focushub

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimerMode { FOCUS, BREAK }

data class TimerUiState(
    val mode: TimerMode = TimerMode.FOCUS,
    val isRunning: Boolean = false,
    val secondsRemaining: Int = FOCUS_SECONDS,
    val totalSecondsForMode: Int = FOCUS_SECONDS,
    val completedFocusSessionsToday: Int = 0,
)

private const val FOCUS_SECONDS = 25 * 60
private const val BREAK_SECONDS = 5 * 60

class FocusHubViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = FocusHubDatabase.getInstance(application).focusSessionDao()

    var timerState by mutableStateOf(TimerUiState())
        private set

    val sessions: StateFlow<List<FocusSession>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var tickerJob: Job? = null

    fun toggleRunning() {
        if (timerState.isRunning) pause() else start()
    }

    private fun start() {
        timerState = timerState.copy(isRunning = true)
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (timerState.secondsRemaining > 0) {
                delay(1000)
                timerState = timerState.copy(secondsRemaining = timerState.secondsRemaining - 1)
            }
            onModeComplete()
        }
    }

    private fun pause() {
        tickerJob?.cancel()
        timerState = timerState.copy(isRunning = false)
    }

    fun reset() {
        tickerJob?.cancel()
        val total = if (timerState.mode == TimerMode.FOCUS) FOCUS_SECONDS else BREAK_SECONDS
        timerState = timerState.copy(isRunning = false, secondsRemaining = total, totalSecondsForMode = total)
    }

    private fun onModeComplete() {
        if (timerState.mode == TimerMode.FOCUS) {
            viewModelScope.launch {
                dao.insert(FocusSession(durationMinutes = FOCUS_SECONDS / 60))
            }
            timerState = timerState.copy(
                mode = TimerMode.BREAK,
                isRunning = false,
                secondsRemaining = BREAK_SECONDS,
                totalSecondsForMode = BREAK_SECONDS,
                completedFocusSessionsToday = timerState.completedFocusSessionsToday + 1,
            )
        } else {
            timerState = timerState.copy(
                mode = TimerMode.FOCUS,
                isRunning = false,
                secondsRemaining = FOCUS_SECONDS,
                totalSecondsForMode = FOCUS_SECONDS,
            )
        }
    }

    fun todaysSessionCount(): Int {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return sessions.value.count { it.completedAt >= startOfDay }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
