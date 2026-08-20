package com.voidecosystem.app.install

import android.content.pm.PackageManager
import com.voidecosystem.core.model.SilentInstallStatus
import rikka.shizuku.Shizuku
import java.io.File

/** Request code passed to [Shizuku.requestPermission]; arbitrary but must be stable. */
const val SHIZUKU_REQUEST_CODE = 4242

/**
 * Optional silent-install path via [Shizuku](https://shizuku.rikka.app) — a
 * system-level "ADB shell, but always-on" broker the user pairs once
 * (wireless debugging or a one-time ADB command) and explicitly grants this
 * app permission to use. When available, installs happen with zero system
 * prompts, matching how the Play Store updates apps in the background.
 *
 * This is strictly a bonus path: [ApkInstaller] always falls back to the
 * normal FileProvider + system installer intent whenever Shizuku isn't
 * paired, isn't running, or permission has been revoked — nothing here is
 * required for the installer to work.
 */
object ShizukuInstaller {

    fun status(): SilentInstallStatus = try {
        if (!Shizuku.pingBinder() || Shizuku.isPreV11()) {
            SilentInstallStatus.UNAVAILABLE
        } else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            SilentInstallStatus.READY
        } else {
            SilentInstallStatus.NEEDS_PERMISSION
        }
    } catch (e: Exception) {
        // Shizuku not installed, binder not bound yet, or any other
        // transient failure — treat all of these as simply unavailable.
        SilentInstallStatus.UNAVAILABLE
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
        } catch (e: Exception) {
            // No-op — caller just won't get the silent path this run.
        }
    }

    /**
     * Runs `pm install` through Shizuku's privileged shell. Returns true
     * only if the process both launched and exited 0; any failure
     * (permission revoked mid-flight, OEM restrictions, low storage, etc.)
     * means the caller should fall back to the normal installer intent.
     */
    fun silentInstall(apkFile: File): Boolean {
        if (status() != SilentInstallStatus.READY) return false
        return try {
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", "pm install -r -d '${apkFile.absolutePath}'"),
                null,
                null,
            )
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
