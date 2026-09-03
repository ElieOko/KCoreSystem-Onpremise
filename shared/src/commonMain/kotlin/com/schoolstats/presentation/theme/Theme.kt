package com.schoolstats.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E7FF),
    secondary = Color(0xFF2E7D32),
    tertiary = Color(0xFF6A1B9A),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF1A1C1E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D2745),
    primaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF81C784),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFECEFF1),
)

@Composable
fun SchoolStatsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
