pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "void-ecosystem"

// ---- Host app shell ----
include(":app")

// ---- Core (shared across every module) ----
include(":core:designsystem")
include(":core:common")
include(":core:model")
include(":core:ui")

// ---- System & Developer Tools ----
include(":feature:dashboard")
include(":feature:theming")
include(":feature:terminal")
include(":feature:sysmonitor")

// ---- AI & Automation ----
include(":feature:omniassistant")
include(":feature:automation")
include(":feature:routines")

// ---- Media & Entertainment ----
// No music player module — Void Music already exists as its own app.
include(":feature:gallery")

// ---- Productivity & Life Tracking ----
include(":feature:focushub")
include(":feature:todo")
include(":feature:journal")
include(":feature:finance")
include(":feature:pantry")

// ---- Core OS Utilities ----
include(":feature:calculator")
include(":feature:notes")
include(":feature:calendar")
include(":feature:filemanager")

// ---- Communication & Connectivity ----
include(":feature:dialer")
include(":feature:browser")
