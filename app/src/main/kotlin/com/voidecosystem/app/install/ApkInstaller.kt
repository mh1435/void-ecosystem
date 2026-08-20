package com.voidecosystem.app.install

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.content.FileProvider
import com.voidecosystem.core.model.AppInstallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val RELEASES_LATEST_BASE = "https://github.com/mh1435/void-ecosystem/releases/latest/download"

/**
 * Downloads a pillar app's release APK straight from this repo's GitHub
 * Release and hands it to the system installer — the in-app equivalent of
 * tapping "Install" in an app store, since these apps aren't published to
 * one. Uses Android's own [DownloadManager] (no extra HTTP dependency) and
 * a [FileProvider] (declared in AndroidManifest.xml) to grant the system
 * installer read access to the downloaded file.
 */
class ApkInstaller(private val context: Context, private val scope: CoroutineScope) {

    /** Keyed by module route (e.g. "calculator"), observed directly by Compose. */
    val states = mutableStateMapOf<String, AppInstallState>()

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun refreshInstalledState(route: String, packageName: String) {
        if (states[route] is AppInstallState.Downloading || states[route] == AppInstallState.Installing) {
            return
        }
        states[route] = if (isInstalled(packageName)) AppInstallState.Installed else AppInstallState.NotInstalled
    }

    private fun isInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    /** True if the user still needs to flip on "install unknown apps" for this app. */
    fun needsInstallPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()

    fun requestInstallPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        )
        context.startActivity(intent)
    }

    fun startDownload(route: String) {
        if (states[route] is AppInstallState.Downloading || states[route] == AppInstallState.Installing) return
        if (needsInstallPermission()) {
            requestInstallPermission()
            return
        }

        val fileName = "void-$route-release.apk"
        val destFile = File(context.getExternalFilesDir(null), fileName)
        if (destFile.exists()) destFile.delete()

        val request = DownloadManager.Request(Uri.parse("$RELEASES_LATEST_BASE/$fileName"))
            .setTitle("Void ${route.replaceFirstChar { it.uppercase() }}")
            .setMimeType("application/vnd.android.package-archive")
            .setDestinationInExternalFilesDir(context, null, fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadId = downloadManager.enqueue(request)
        states[route] = AppInstallState.Downloading(0f)

        scope.launch(Dispatchers.IO) {
            pollProgress(downloadId, route, destFile)
        }
    }

    private suspend fun pollProgress(downloadId: Long, route: String, destFile: File) {
        var downloading = true
        while (downloading) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
            if (cursor == null) {
                downloading = false
                states[route] = AppInstallState.NotInstalled
                continue
            }
            cursor.use {
                if (!it.moveToFirst()) {
                    downloading = false
                    states[route] = AppInstallState.NotInstalled
                    return@use
                }
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        downloading = false
                        states[route] = AppInstallState.Installing
                        triggerInstall(destFile)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        downloading = false
                        states[route] = AppInstallState.NotInstalled
                    }
                    else -> {
                        val bytes = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val progress = if (total > 0) bytes.toFloat() / total.toFloat() else 0f
                        states[route] = AppInstallState.Downloading(progress)
                    }
                }
            }
            if (downloading) delay(400)
        }
    }

    private fun triggerInstall(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
