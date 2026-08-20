package com.voidecosystem.core.model

/**
 * Shared, cross-module description of one app in the ecosystem. The
 * dashboard renders a grid of these; each feature module contributes one
 * via its own registry entry so :app never hard-codes feature metadata.
 */
data class EcosystemModule(
    val route: String,
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
