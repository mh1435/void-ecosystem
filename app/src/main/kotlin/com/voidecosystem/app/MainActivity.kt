package com.voidecosystem.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.voidecosystem.core.designsystem.theme.VoidTheme
import com.voidecosystem.feature.dashboard.DashboardRoute

private const val RELEASES_URL = "https://github.com/mh1435/void-ecosystem/releases"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoidTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardRoute(onModuleClick = ::launchOrOfferInstall)
                }
            }
        }
    }

    /**
     * Every pillar app is a separately installed APK, not an in-process
     * screen — so tapping a dashboard tile either hands off to that app
     * via its launch intent, or, if it isn't installed yet, opens the
     * GitHub Releases page where it can be downloaded.
     */
    private fun launchOrOfferInstall(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
            return
        }
        Toast.makeText(this, "Not installed yet — opening the Releases page", Toast.LENGTH_SHORT).show()
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_URL)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No browser available to open $RELEASES_URL", Toast.LENGTH_LONG).show()
        }
    }
}
