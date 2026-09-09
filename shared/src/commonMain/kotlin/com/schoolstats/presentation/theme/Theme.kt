package com.schoolstats.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F3D68),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E6F5),
    onPrimaryContainer = Color(0xFF0B2744),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFC9A227),
    onTertiary = Color(0xFF2A2100),
    background = Color(0xFFF4F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EEF5),
    onSurface = Color(0xFF122033),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFB91C1C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF0B2744),
    primaryContainer = Color(0xFF163A5F),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF042F2E),
    surface = Color(0xFF0F172A),
    background = Color(0xFF0B1220),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B),
    tertiary = Color(0xFFE8C547),
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
