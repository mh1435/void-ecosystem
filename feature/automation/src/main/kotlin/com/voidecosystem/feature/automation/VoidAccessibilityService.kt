package com.voidecosystem.feature.automation

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityEvent

class VoidAccessibilityService : AccessibilityService() {

    private lateinit var prefs: SharedPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (!::prefs.isInitialized) prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val blocked = prefs.getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet()
        if (packageName in blocked) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}

    companion object {
        const val PREFS_NAME = "void_automation_prefs"
        const val KEY_BLOCKED_APPS = "blocked_apps"
    }
}
