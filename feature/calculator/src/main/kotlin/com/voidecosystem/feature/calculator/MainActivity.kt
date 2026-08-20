package com.voidecosystem.feature.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.voidecosystem.core.designsystem.theme.VoidTheme

/** Standalone entry point — this module ships as its own installable app. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoidTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalculatorRoute(onBack = { finish() })
                }
            }
        }
    }
}
