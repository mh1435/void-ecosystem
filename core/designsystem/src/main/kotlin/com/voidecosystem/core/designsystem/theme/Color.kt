package com.voidecosystem.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Void Ecosystem palette — a single dark-first identity shared by every
// module so the suite reads as one OS, not twenty separate apps.
val VoidBackground = Color(0xFF0B0B10)
val VoidSurface = Color(0xFF15151D)
val VoidSurfaceElevated = Color(0xFF1E1E29)
val VoidOnSurface = Color(0xFFEDEDF5)
val VoidOnSurfaceMuted = Color(0xFFA0A0B2)
val VoidOutline = Color(0xFF2C2C3A)

val VoidPrimary = Color(0xFF7C5CFF)
val VoidPrimaryContainer = Color(0xFF2A1F66)
val VoidOnPrimary = Color(0xFFFFFFFF)

val VoidSecondary = Color(0xFF00D9C0)
val VoidError = Color(0xFFFF5C7A)

// Per-pillar accent colors, cycled across dashboard tiles and feature
// module headers so each pillar keeps a recognizable identity.
val PillarAccents = listOf(
    Color(0xFF7C5CFF), // System & Dev Tools
    Color(0xFF00D9C0), // AI & Automation
    Color(0xFFFF9F5C), // Media & Entertainment
    Color(0xFF5CC8FF), // Productivity & Life Tracking
    Color(0xFFFF5C9E), // Core OS Utilities
    Color(0xFF9EFF5C), // Communication & Connectivity
    Color(0xFFFFD65C),
    Color(0xFFB05CFF),
    Color(0xFF5CFFB0),
    Color(0xFFFF7A5C),
)
