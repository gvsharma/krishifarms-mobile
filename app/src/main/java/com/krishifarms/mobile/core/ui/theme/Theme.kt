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
    primary = CanopiaGreen,
    onPrimary = Color.White,
    primaryContainer = CanopiaMint,
    onPrimaryContainer = CanopiaGreen,
    secondary = CanopiaTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB7E4DB),
    onSecondaryContainer = Color(0xFF0D3D36),
    tertiary = CanopiaLime,
    onTertiary = CanopiaForestText,
    tertiaryContainer = Color(0xFFE8F5C8),
    onTertiaryContainer = Color(0xFF2D3A0F),
    background = CanopiaSageBackground,
    onBackground = CanopiaForestText,
    surface = CanopiaSurface,
    onSurface = CanopiaForestText,
    surfaceVariant = CanopiaSurfaceVariant,
    onSurfaceVariant = CanopiaMutedText,
    outline = CanopiaOutline,
    outlineVariant = Color(0xFFD4DDD4),
    error = CanopiaError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = CanopiaDarkPrimary,
    onPrimary = CanopiaDarkBackground,
    primaryContainer = Color(0xFF1B4332),
    onPrimaryContainer = CanopiaMint,
    secondary = Color(0xFF6DD5C4),
    onSecondary = CanopiaDarkBackground,
    secondaryContainer = Color(0xFF0D3D36),
    onSecondaryContainer = Color(0xFFB7E4DB),
    tertiary = CanopiaLime,
    onTertiary = CanopiaDarkBackground,
    tertiaryContainer = Color(0xFF3D4F1A),
    onTertiaryContainer = Color(0xFFE8F5C8),
    background = CanopiaDarkBackground,
    onBackground = CanopiaDarkOnSurface,
    surface = CanopiaDarkSurface,
    onSurface = CanopiaDarkOnSurface,
    surfaceVariant = CanopiaDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9EAE9E),
    outline = Color(0xFF4A5A4A),
    outlineVariant = Color(0xFF2E3A2E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
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
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = KrishiFarmsShapes,
        content = content,
    )
}
