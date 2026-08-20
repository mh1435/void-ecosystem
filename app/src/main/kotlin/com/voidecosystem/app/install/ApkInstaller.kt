package com.voidecosystem.app.install

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.content.FileProvider
import com.voidecosystem.app.BuildConfig
import com.voidecosystem.core.model.AppInstallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val RELEASES_LATEST_BASE = "https://github.com/mh1435/void-ecosystem/releases/latest/download"
private const val TAG = "ApkInstaller"
private const val MAX_REDIRECTS = 5
private const val CONNECT_TIMEOUT_MS = 15_000
private const val READ_TIMEOUT_MS = 15_000

/**
 * Downloads a pillar app's release APK straight from this repo's GitHub
 * Release and hands it to the system installer — the in-app equivalent of
 * tapping "Install" in an app store, since these apps aren't published to
 * one.
 *
 * Fetches over a plain [HttpURLConnection] on a background coroutine
 * instead of Android's `DownloadManager`. `DownloadManager` doesn't do the
 * transfer itself — it hands the request off to a separate system process
 * (`com.android.providers.downloads`), which a number of OEM skins (MIUI
 * and others) let users disable, freeze, or battery-restrict independently
 * of this app. When that happens every request just sits at
 * PENDING/RUNNING with zero byte movement forever, even though the device's
 * actual connection is fine — confirmed here: the release asset downloads
 * instantly from outside the app, but never progressed through
 * `DownloadManager`. Doing the HTTP request ourselves removes that whole
 * failure class — it's just this app's own network I/O, same as any other
 * request it makes, with nothing else on the device able to silently starve it.
 */
class ApkInstaller(private val context: Context, private val scope: CoroutineScope) {

    /** Keyed by module route (e.g. "calculator"), observed directly by Compose. */
    val states = mutableStateMapOf<String, AppInstallState>()

    fun refreshInstalledState(route: String, packageName: String) {
        if (states[route] is AppInstallState.Downloading || states[route] == AppInstallState.Installing) {
            return
        }
        val installedVersion = installedVersionCode(packageName)
        val latestVersion = BuildConfig.VERSION_CODE.toLong()
        states[route] = when {
            installedVersion == null -> AppInstallState.NotInstalled
            installedVersion < latestVersion -> AppInstallState.UpdateAvailable(installedVersion, latestVersion)
            else -> AppInstallState.Installed
        }
    }

    /**
     * Every pillar app and this installer itself share one versionCode
     * scheme (the git commit count at build time — see root build.gradle.kts),
     * so this installer's own [BuildConfig.VERSION_CODE] doubles as "the
     * latest known ecosystem version" without a separate network call to
     * check for updates.
     */
    private fun installedVersionCode(packageName: String): Long? = try {
        val info = context.packageManager.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    /** True if the user still needs to flip on "install unknown apps" for this app. */
    fun needsInstallPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()

    fun requestInstallPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun startDownload(route: String) {
        if (states[route] is AppInstallState.Downloading || states[route] == AppInstallState.Installing) return
        if (needsInstallPermission()) {
            requestInstallPermission()
            return
        }

        states[route] = AppInstallState.Downloading(0f)
        scope.launch(Dispatchers.IO) {
            downloadAndInstall(route)
        }
    }

    private fun downloadAndInstall(route: String) {
        val fileName = "void-$route-release.apk"
        val destFile = File(context.getExternalFilesDir(null), fileName)
        val tempFile = File(context.getExternalFilesDir(null), "$fileName.part")
        if (tempFile.exists()) tempFile.delete()

        var connection: HttpURLConnection? = null
        try {
            var url = URL("$RELEASES_LATEST_BASE/$fileName")
            var redirects = 0
            while (true) {
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    requestMethod = "GET"
                }
                val code = connection.responseCode
                if (code in 300..399) {
                    val location = connection.getHeaderField("Location")
                        ?: throw IOException("Redirect from $url had no Location header")
                    connection.disconnect()
                    if (++redirects > MAX_REDIRECTS) throw IOException("Too many redirects fetching $fileName")
                    url = URL(location)
                    continue
                }
                if (code != HttpURLConnection.HTTP_OK) {
                    throw IOException("HTTP $code fetching $url")
                }
                break
            }

            val total = connection.contentLengthLong
            var bytesRead = 0L
            var lastReportedAt = 0L
            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        bytesRead += read
                        val now = System.currentTimeMillis()
                        if (now - lastReportedAt > 150) {
                            lastReportedAt = now
                            val progress = if (total > 0) bytesRead.toFloat() / total.toFloat() else 0f
                            states[route] = AppInstallState.Downloading(progress.coerceIn(0f, 1f))
                        }
                    }
                }
            }

            if (destFile.exists()) destFile.delete()
            if (!tempFile.renameTo(destFile)) {
                tempFile.copyTo(destFile, overwrite = true)
                tempFile.delete()
            }

            states[route] = AppInstallState.Installing
            triggerInstall(destFile)
        } catch (e: Exception) {
            Log.e(TAG, "$route: download failed", e)
            tempFile.delete()
            states[route] = AppInstallState.NotInstalled
        } finally {
            connection?.disconnect()
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
