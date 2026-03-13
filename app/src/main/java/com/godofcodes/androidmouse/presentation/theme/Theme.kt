package com.godofcodes.androidmouse.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Dark palette ──────────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary              = Color(0xFFCDD2D8),   // vivid silver-gray
    onPrimary            = Color(0xFF0F1117),
    primaryContainer     = Color(0xFF2E3239),
    onPrimaryContainer   = Color(0xFFECEEF2),

    secondary            = Color(0xFF9AA3AD),   // muted silver
    onSecondary          = Color(0xFF0F1117),
    secondaryContainer   = Color(0xFF252830),
    onSecondaryContainer = Color(0xFFD4D8DE),

    background           = Color(0xFF0F1117),
    onBackground         = Color(0xFFE8EAF0),

    surface              = Color(0xFF1C1E26),
    onSurface            = Color(0xFFE8EAF0),
    surfaceVariant       = Color(0xFF262A35),
    onSurfaceVariant     = Color(0xFFB0B5C8),

    outline              = Color(0xFF3A3F52),
    outlineVariant       = Color(0xFF2C3044),

    error                = Color(0xFFFF6B6B),
    onError              = Color(0xFF1A0000),
)

// ── Light palette ─────────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary              = Color(0xFF4A5260),   // dark cool gray
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFDDE0E6),
    onPrimaryContainer   = Color(0xFF0F1117),

    secondary            = Color(0xFF6B7685),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFE8EAED),
    onSecondaryContainer = Color(0xFF1C1E26),

    background           = Color(0xFFF2F3F5),
    onBackground         = Color(0xFF0F1117),

    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF0F1117),
    surfaceVariant       = Color(0xFFE4E6ED),
    onSurfaceVariant     = Color(0xFF4A4E60),

    outline              = Color(0xFFB0B5C8),
    outlineVariant       = Color(0xFFD0D3E0),

    error                = Color(0xFFD32F2F),
    onError              = Color.White,
)

@Composable
fun AndroidMouseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
