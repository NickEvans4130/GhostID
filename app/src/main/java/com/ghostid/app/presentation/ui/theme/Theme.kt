package com.ghostid.app.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ghostid.app.presentation.viewmodel.AppTheme

private val GhostPurple = Color(0xFF6C63FF)
private val GhostRed = Color(0xFFE94560)
private val GhostDark = Color(0xFF0D0D0D)
private val GhostSurface = Color(0xFF1A1A2E)
private val GhostSurfaceVariant = Color(0xFF16213E)
private val GhostGreen = Color(0xFF00FF88)

private val DarkColorScheme = darkColorScheme(
    primary = GhostPurple,
    secondary = GhostRed,
    tertiary = GhostGreen,
    background = GhostDark,
    surface = GhostSurface,
    surfaceVariant = GhostSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = Color.Black,
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF111111),
)

private val LightColorScheme = lightColorScheme(
    primary = GhostPurple,
    secondary = GhostRed,
    tertiary = Color(0xFF00C9A7),
    background = Color(0xFFF8F8FF),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
)

@Composable
fun GhostIDTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.AMOLED -> AmoledColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
