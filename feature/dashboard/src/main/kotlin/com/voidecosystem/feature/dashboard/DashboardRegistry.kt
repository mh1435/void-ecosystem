package com.voidecosystem.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector
import com.voidecosystem.core.model.EcosystemModule
import com.voidecosystem.core.model.Pillar

/**
 * The dashboard's contract with every pillar app: each one is a separate
 * installed APK (applicationId `com.voidecosystem.<name>`), so this
 * registry never imports feature module code — it only knows package
 * names, display copy, and icons. Tapping a tile launches that package or,
 * if it isn't installed, opens the Releases page (see :app's MainActivity).
 * No Music Player or AI Assistant entries here — Void Music and Void AI
 * already exist as their own apps.
 */
data class DashboardTile(
    val module: EcosystemModule,
    val icon: ImageVector,
)

val DashboardRegistry: List<DashboardTile> = listOf(
    DashboardTile(
        EcosystemModule("theming", "com.voidecosystem.theming", "Theming Engine", "Palette preview & wallpapers", Pillar.SYSTEM_TOOLS),
        Icons.Filled.Palette,
    ),
    DashboardTile(
        EcosystemModule("terminal", "com.voidecosystem.terminal", "Terminal Sandbox", "Sandboxed shell", Pillar.SYSTEM_TOOLS),
        Icons.Filled.Terminal,
    ),
    DashboardTile(
        EcosystemModule("sysmonitor", "com.voidecosystem.sysmonitor", "System Monitor", "RAM, CPU, battery", Pillar.SYSTEM_TOOLS),
        Icons.Filled.Analytics,
    ),
    DashboardTile(
        EcosystemModule("automation", "com.voidecosystem.automation", "Automation", "Accessibility app blocker", Pillar.AI_AUTOMATION),
        Icons.Filled.TouchApp,
    ),
    DashboardTile(
        EcosystemModule("routines", "com.voidecosystem.routines", "Routine Scheduler", "Context-aware device states", Pillar.AI_AUTOMATION),
        Icons.Filled.Schedule,
    ),
    DashboardTile(
        EcosystemModule("gallery", "com.voidecosystem.gallery", "Smart Gallery", "Tagging & private vault", Pillar.MEDIA),
        Icons.Filled.Photo,
    ),
    DashboardTile(
        EcosystemModule("focushub", "com.voidecosystem.focushub", "Focus Hub", "Gamified focus timer", Pillar.PRODUCTIVITY),
        Icons.Filled.LocalFireDepartment,
    ),
    DashboardTile(
        EcosystemModule("todo", "com.voidecosystem.todo", "To-Do", "Projects & due dates", Pillar.PRODUCTIVITY),
        Icons.Filled.Checklist,
    ),
    DashboardTile(
        EcosystemModule("journal", "com.voidecosystem.journal", "Journal", "Private Markdown journaling", Pillar.PRODUCTIVITY),
        Icons.Filled.MenuBook,
    ),
    DashboardTile(
        EcosystemModule("finance", "com.voidecosystem.finance", "Finance", "Personal finance tracker", Pillar.PRODUCTIVITY),
        Icons.Filled.Savings,
    ),
    DashboardTile(
        EcosystemModule("pantry", "com.voidecosystem.pantry", "Pantry & Flavor", "Meal prep & flavor logs", Pillar.PRODUCTIVITY),
        Icons.Filled.Kitchen,
    ),
    DashboardTile(
        EcosystemModule("calculator", "com.voidecosystem.calculator", "Calculator", "Scientific & conversions", Pillar.UTILITIES),
        Icons.Filled.Calculate,
    ),
    DashboardTile(
        EcosystemModule("notes", "com.voidecosystem.notes", "Notes & Voice", "Markdown notes, recordings", Pillar.UTILITIES),
        Icons.Filled.Mic,
    ),
    DashboardTile(
        EcosystemModule("calendar", "com.voidecosystem.calendar", "Calendar", "Recurring events", Pillar.UTILITIES),
        Icons.Filled.CalendarMonth,
    ),
    DashboardTile(
        EcosystemModule("filemanager", "com.voidecosystem.filemanager", "File Manager", "Root-capable explorer", Pillar.UTILITIES),
        Icons.Filled.Folder,
    ),
    DashboardTile(
        EcosystemModule("dialer", "com.voidecosystem.dialer", "Dialer & Contacts", "Dial pad & contact list", Pillar.COMMUNICATION),
        Icons.Filled.Call,
    ),
    DashboardTile(
        EcosystemModule("browser", "com.voidecosystem.browser", "Browser", "Privacy-first browsing", Pillar.COMMUNICATION),
        Icons.Filled.Public,
    ),
)
