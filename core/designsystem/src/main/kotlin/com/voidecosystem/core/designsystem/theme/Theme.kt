package com.voidecosystem.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VoidDarkColorScheme = darkColorScheme(
    primary = VoidPrimary,
    onPrimary = VoidOnPrimary,
    primaryContainer = VoidPrimaryContainer,
    secondary = VoidSecondary,
    background = VoidBackground,
    surface = VoidSurface,
    surfaceVariant = VoidSurfaceElevated,
    onBackground = VoidOnSurface,
    onSurface = VoidOnSurface,
    onSurfaceVariant = VoidOnSurfaceMuted,
    outline = VoidOutline,
    error = VoidError,
)

private val VoidLightColorScheme = lightColorScheme(
    primary = VoidPrimary,
    secondary = VoidSecondary,
    error = VoidError,
)

/**
 * Single theme entry point every module (dashboard, feature screens, and
 * the host app) wraps its content in, so the whole ecosystem shares one
 * color scheme, type scale, and shape system.
 */
@Composable
fun VoidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) VoidDarkColorScheme else VoidLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VoidTypography,
        content = content,
    )
}
