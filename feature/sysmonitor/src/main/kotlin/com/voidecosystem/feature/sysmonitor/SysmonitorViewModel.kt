package com.voidecosystem.feature.sysmonitor

import android.app.ActivityManager
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Process
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SysmonitorUiState(
    val totalRamMb: Long = 0,
    val availRamMb: Long = 0,
    val usedRamPercent: Int = 0,
    val lowMemory: Boolean = false,
    val lowMemThresholdMb: Long = 0,
    val cpuCores: Int = Runtime.getRuntime().availableProcessors(),
    val appCpuTimeMs: Long = 0,
    val appHeapUsedMb: Long = 0,
    val appHeapMaxMb: Long = 0,
    val batteryPercent: Int = 0,
    val batteryStatus: String = "Unknown",
    val batteryHealth: String = "Unknown",
    val temperatureC: Float = 0f,
    val voltageMv: Int = 0,
    val runningProcessCount: Int = 0,
)

/**
 * Real device telemetry: system RAM via [ActivityManager.MemoryInfo], this
 * process's JVM heap and CPU time, and battery status via the sticky
 * ACTION_BATTERY_CHANGED broadcast. Android does not expose per-process
 * system-wide CPU% or other apps' memory/process lists to third-party apps
 * without root — those numbers are only ever available from Android's own
 * system UI — so this reports what's genuinely obtainable rather than
 * faking a system-wide view.
 */
class SysmonitorViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(SysmonitorUiState())
        private set

    private val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val percent = if (level >= 0 && scale > 0) (level * 100) / scale else uiState.batteryPercent
            val status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                else -> "Unknown"
            }
            val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }
            val tempC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
            val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            uiState = uiState.copy(
                batteryPercent = percent,
                batteryStatus = status,
                batteryHealth = health,
                temperatureC = tempC,
                voltageMv = voltageMv,
            )
        }
    }

    init {
        ContextCompat.registerReceiver(
            application,
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        viewModelScope.launch {
            while (true) {
                refreshMemoryAndCpu()
                delay(2000)
            }
        }
    }

    private fun refreshMemoryAndCpu() {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedPercent = if (totalMb > 0) (((totalMb - availMb) * 100) / totalMb).toInt() else 0

        val runtime = Runtime.getRuntime()
        val heapUsedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val heapMaxMb = runtime.maxMemory() / (1024 * 1024)

        val runningProcesses = activityManager.runningAppProcesses?.size ?: 0

        uiState = uiState.copy(
            totalRamMb = totalMb,
            availRamMb = availMb,
            usedRamPercent = usedPercent,
            lowMemory = memInfo.lowMemory,
            lowMemThresholdMb = memInfo.threshold / (1024 * 1024),
            appCpuTimeMs = Process.getElapsedCpuTime(),
            appHeapUsedMb = heapUsedMb,
            appHeapMaxMb = heapMaxMb,
            runningProcessCount = runningProcesses,
        )
    }

    override fun onCleared() {
        super.onCleared()
        runCatching { getApplication<Application>().unregisterReceiver(batteryReceiver) }
    }
}
