package com.voidecosystem.feature.routines

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class RoutinesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = RoutinesDatabase.getInstance(application).scheduleDao()
    private val notificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val schedules: StateFlow<List<RoutineSchedule>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var hasDndAccess by mutableStateOf(notificationManager.isNotificationPolicyAccessGranted)
        private set

    var lastAppliedMessage by mutableStateOf<String?>(null)
        private set

    fun refreshPermission() {
        hasDndAccess = notificationManager.isNotificationPolicyAccessGranted
    }

    fun addSchedule(label: String, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        if (label.isBlank()) return
        viewModelScope.launch {
            dao.upsert(
                RoutineSchedule(
                    label = label.trim(),
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                ),
            )
        }
    }

    fun toggleEnabled(schedule: RoutineSchedule) {
        viewModelScope.launch { dao.update(schedule.copy(isEnabled = !schedule.isEnabled)) }
    }

    fun delete(schedule: RoutineSchedule) {
        viewModelScope.launch { dao.deleteById(schedule.id) }
    }

    /**
     * Checks the current time against every enabled schedule and toggles
     * system Do Not Disturb to match. There's no background enforcement
     * yet (that needs a WorkManager/AlarmManager job) — this applies the
     * current state on demand, e.g. when the user opens the app or taps
     * "Apply now".
     */
    fun applyNow() {
        if (!hasDndAccess) return
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val active = dao.getEnabled().any { schedule ->
                if (schedule.startMinutesOfDay <= schedule.endMinutesOfDay) {
                    nowMinutes in schedule.startMinutesOfDay until schedule.endMinutesOfDay
                } else {
                    // Overnight range, e.g. 22:00–06:00.
                    nowMinutes >= schedule.startMinutesOfDay || nowMinutes < schedule.endMinutesOfDay
                }
            }
            notificationManager.setInterruptionFilter(
                if (active) NotificationManager.INTERRUPTION_FILTER_PRIORITY else NotificationManager.INTERRUPTION_FILTER_ALL,
            )
            lastAppliedMessage = if (active) "Focus mode is now ON" else "Focus mode is now OFF"
        }
    }
}
