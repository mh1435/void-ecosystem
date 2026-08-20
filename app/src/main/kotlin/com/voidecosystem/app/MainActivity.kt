package com.voidecosystem.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.voidecosystem.app.navigation.VoidNavHost
import com.voidecosystem.core.designsystem.theme.VoidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoidTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VoidNavHost()
                }
            }
        }
    }
}
