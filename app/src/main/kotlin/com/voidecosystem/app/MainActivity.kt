package com.voidecosystem.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.voidecosystem.app.install.ApkInstaller
import com.voidecosystem.app.install.ShizukuInstaller
import com.voidecosystem.core.designsystem.theme.VoidTheme
import com.voidecosystem.core.model.AppInstallState
import com.voidecosystem.core.model.SilentInstallStatus
import com.voidecosystem.feature.dashboard.DashboardRegistry
import com.voidecosystem.feature.dashboard.DashboardRoute
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private lateinit var installer: ApkInstaller
    private var silentInstallStatus by mutableStateOf(SilentInstallStatus.UNAVAILABLE)

    private val shizukuPermissionListener =
        Shizuku.OnRequestPermissionResultListener { _, _ -> refreshSilentInstallStatus() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installer = ApkInstaller(applicationContext, lifecycleScope)
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)

        setContent {
            VoidTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardRoute(
                        installStates = installer.states,
                        onModuleClick = ::handleTileTap,
                        onUpdateAll = ::updateAll,
                        silentInstallStatus = silentInstallStatus,
                        onEnableSilentInstalls = { ShizukuInstaller.requestPermission() },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Catches installs/uninstalls that happened while this activity was
        // backgrounded (e.g. the user just finished installing one of the
        // pillar apps and switched back).
        DashboardRegistry.forEach { tile ->
            installer.refreshInstalledState(tile.module.route, tile.module.packageName)
        }
        refreshSilentInstallStatus()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        super.onDestroy()
    }

    private fun refreshSilentInstallStatus() {
        silentInstallStatus = ShizukuInstaller.status()
    }

    private fun updateAll() {
        DashboardRegistry.forEach { tile ->
            if (installer.states[tile.module.route] is AppInstallState.UpdateAvailable) {
                installer.startDownload(tile.module.route)
            }
        }
    }

    private fun handleTileTap(route: String, packageName: String) {
        when (installer.states[route]) {
            AppInstallState.Installed -> {
                packageManager.getLaunchIntentForPackage(packageName)?.let(::startActivity)
            }
            is AppInstallState.Downloading, AppInstallState.Installing -> {
                // Already in progress — ignore the tap rather than starting a second download.
            }
            AppInstallState.NotInstalled, is AppInstallState.UpdateAvailable, null -> {
                installer.startDownload(route)
            }
        }
    }
}
