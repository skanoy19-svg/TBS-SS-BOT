package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF001F26),
    primaryContainer = Color(0xFF00363F),
    onPrimaryContainer = Color(0xFF99F2FF),
    secondary = SignalGreen,
    onSecondary = Color(0xFF003915),
    secondaryContainer = Color(0xFF005322),
    onSecondaryContainer = Color(0xFF6BFF8B),
    tertiary = SignalGold,
    onTertiary = Color(0xFF402D00),
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = SurfaceBorderHighlight,
    error = SignalRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Trading terminals look best in persistent high-contrast dark theme
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
