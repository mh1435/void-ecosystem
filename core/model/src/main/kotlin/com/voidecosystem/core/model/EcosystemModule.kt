package com.voidecosystem.core.model

/**
 * Shared, cross-module description of one app in the ecosystem. Every
 * pillar app is its own separately installed APK — [packageName] is its
 * applicationId, which the dashboard uses to launch it (or, if it isn't
 * installed, to point the user at where to get it).
 */
data class EcosystemModule(
    val route: String,
    val packageName: String,
    val title: String,
    val subtitle: String,
    val pillar: Pillar,
)

enum class Pillar(val displayName: String) {
    SYSTEM_TOOLS("System & Developer Tools"),
    AI_AUTOMATION("AI & Automation"),
    MEDIA("Media & Entertainment"),
    PRODUCTIVITY("Productivity & Life Tracking"),
    UTILITIES("Core OS Utilities"),
    COMMUNICATION("Communication & Connectivity"),
}
