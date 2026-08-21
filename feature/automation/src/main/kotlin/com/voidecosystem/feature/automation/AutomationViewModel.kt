package com.voidecosystem.feature.automation

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BlockableApp(
    val packageName: String,
    val label: String,
    val isBlocked: Boolean,
)

class AutomationViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(VoidAccessibilityService.PREFS_NAME, Application.MODE_PRIVATE)

    var isServiceEnabled by mutableStateOf(false)
        private set

    var apps by mutableStateOf<List<BlockableApp>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun refresh() {
        isServiceEnabled = isAccessibilityServiceEnabled()
        loadApps()
    }

    private fun loadApps() {
        isLoading = true
        viewModelScope.launch {
            apps = withContext(Dispatchers.IO) { queryLaunchableApps() }
            isLoading = false
        }
    }

    fun toggleBlocked(app: BlockableApp) {
        val blocked = prefs.getStringSet(VoidAccessibilityService.KEY_BLOCKED_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (app.packageName in blocked) blocked.remove(app.packageName) else blocked.add(app.packageName)
        prefs.edit().putStringSet(VoidAccessibilityService.KEY_BLOCKED_APPS, blocked).apply()
        apps = apps.map { if (it.packageName == app.packageName) it.copy(isBlocked = !it.isBlocked) else it }
    }

    private fun queryLaunchableApps(): List<BlockableApp> {
        val context = getApplication<Application>()
        val pm = context.packageManager
        val blocked = prefs.getStringSet(VoidAccessibilityService.KEY_BLOCKED_APPS, emptySet()) ?: emptySet()
        val ownPackage = context.packageName
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved
            .asSequence()
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .filter { (packageName, _) -> packageName != ownPackage }
            .distinctBy { it.first }
            .map { (packageName, label) -> BlockableApp(packageName, label, packageName in blocked) }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val context = getApplication<Application>()
        val am = context.getSystemService(AccessibilityManager::class.java) ?: return false
        val expectedId = "${context.packageName}/${VoidAccessibilityService::class.java.name}"
        return am.getEnabledAccessibilityServiceList(AccessibilityManager.FEEDBACK_ALL_MASK)
            .any { it.id == expectedId }
    }
}
