package com.voidecosystem.core.model

/**
 * Where one pillar app stands relative to the dashboard's in-app
 * downloader/installer. Produced by :app (which owns the PackageManager
 * and DownloadManager), rendered by :feature:dashboard (which stays free
 * of Android system dependencies).
 */
sealed interface AppInstallState {
    data object Installed : AppInstallState
    data object NotInstalled : AppInstallState
    data class Downloading(val progress: Float) : AppInstallState
    data object Installing : AppInstallState
}
