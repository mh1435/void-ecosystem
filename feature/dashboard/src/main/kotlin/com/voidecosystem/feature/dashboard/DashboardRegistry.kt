package com.voidecosystem.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
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
 * The dashboard's contract with every feature module: a route string (the
 * feature module owns the matching `<Name>Destination.ROUTE` constant),
 * display copy, and an icon. :app's NavHost is the single source of truth
 * that resolves each route to a real composable — this registry never
 * imports feature module code, only agreed-upon route strings, so the
 * dashboard builds and previews independent of the other 19 modules.
 */
data class DashboardTile(
    val module: EcosystemModule,
    val icon: ImageVector,
)

val DashboardRegistry: List<DashboardTile> = listOf(
    DashboardTile(
        EcosystemModule("theming", "Theming Engine", "Widgets, clocks, lock screen", Pillar.SYSTEM_TOOLS),
        Icons.Filled.Palette,
    ),
    DashboardTile(
        EcosystemModule("terminal", "Terminal Sandbox", "Scripts & API configs", Pillar.SYSTEM_TOOLS),
        Icons.Filled.Terminal,
    ),
    DashboardTile(
        EcosystemModule("sysmonitor", "System Monitor", "RAM, CPU, battery", Pillar.SYSTEM_TOOLS),
        Icons.Filled.Analytics,
    ),
    DashboardTile(
        EcosystemModule("omniassistant", "Omni-Assistant", "Multi-LLM with fallback", Pillar.AI_AUTOMATION),
        Icons.Filled.AutoAwesome,
    ),
    DashboardTile(
        EcosystemModule("automation", "Automation", "Accessibility workflows", Pillar.AI_AUTOMATION),
        Icons.Filled.TouchApp,
    ),
    DashboardTile(
        EcosystemModule("routines", "Routine Scheduler", "Context-aware device states", Pillar.AI_AUTOMATION),
        Icons.Filled.Schedule,
    ),
    DashboardTile(
        EcosystemModule("musicplayer", "Music Player", "Playback & mood playlists", Pillar.MEDIA),
        Icons.Filled.MusicNote,
    ),
    DashboardTile(
        EcosystemModule("gallery", "Smart Gallery", "Tagging & private vault", Pillar.MEDIA),
        Icons.Filled.Photo,
    ),
    DashboardTile(
        EcosystemModule("focushub", "Focus Hub", "Gamified focus timer", Pillar.PRODUCTIVITY),
        Icons.Filled.LocalFireDepartment,
    ),
    DashboardTile(
        EcosystemModule("todo", "To-Do", "Projects & due dates", Pillar.PRODUCTIVITY),
        Icons.Filled.Checklist,
    ),
    DashboardTile(
        EcosystemModule("journal", "Journal", "Private Markdown journaling", Pillar.PRODUCTIVITY),
        Icons.Filled.MenuBook,
    ),
    DashboardTile(
        EcosystemModule("finance", "Finance", "Personal finance tracker", Pillar.PRODUCTIVITY),
        Icons.Filled.Savings,
    ),
    DashboardTile(
        EcosystemModule("pantry", "Pantry & Flavor", "Meal prep & flavor logs", Pillar.PRODUCTIVITY),
        Icons.Filled.Kitchen,
    ),
    DashboardTile(
        EcosystemModule("calculator", "Calculator", "Scientific & conversions", Pillar.UTILITIES),
        Icons.Filled.Calculate,
    ),
    DashboardTile(
        EcosystemModule("notes", "Notes & Voice", "Markdown notes, recordings", Pillar.UTILITIES),
        Icons.Filled.Mic,
    ),
    DashboardTile(
        EcosystemModule("calendar", "Calendar", "Recurring events", Pillar.UTILITIES),
        Icons.Filled.CalendarMonth,
    ),
    DashboardTile(
        EcosystemModule("filemanager", "File Manager", "Root-capable explorer", Pillar.UTILITIES),
        Icons.Filled.Folder,
    ),
    DashboardTile(
        EcosystemModule("dialer", "Dialer & Contacts", "Spam-filtered calling", Pillar.COMMUNICATION),
        Icons.Filled.Call,
    ),
    DashboardTile(
        EcosystemModule("browser", "Browser", "Privacy-first browsing", Pillar.COMMUNICATION),
        Icons.Filled.Public,
    ),
)
