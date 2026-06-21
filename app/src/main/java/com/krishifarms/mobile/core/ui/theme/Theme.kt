package com.krishifarms.mobile.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnGreenPrimary,
    primaryContainer = GreenPrimaryLight,
    onPrimaryContainer = OnSurfaceDark,
    secondary = GreenSecondary,
    onSecondary = OnGreenPrimary,
    tertiary = GreenTertiary,
    background = FieldBackground,
    onBackground = OnSurfaceDark,
    surface = SurfaceLight,
    onSurface = OnSurfaceDark,
    error = ErrorRed,
    onError = OnGreenPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = OnSurfaceDark,
    primaryContainer = GreenPrimaryDark,
    onPrimaryContainer = OnGreenPrimary,
    secondary = GreenSecondary,
    onSecondary = OnGreenPrimary,
    tertiary = HarvestGold,
    background = OnSurfaceDark,
    onBackground = SurfaceLight,
    surface = Color(0xFF1A1C18),
    onSurface = SurfaceLight,
    error = ErrorRed,
    onError = OnGreenPrimary,
)

@Composable
fun KrishiFarmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
