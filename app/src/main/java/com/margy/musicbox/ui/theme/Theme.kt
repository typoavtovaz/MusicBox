package com.margy.musicbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF7C5CFF)
private val AccentSoft = Color(0xFFB3A1FF)
private val Bg = Color(0xFF0B0D11)
private val Surface = Color(0xFF14171E)
private val SurfaceHi = Color(0xFF1D2129)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = AccentSoft,
    secondary = Color(0xFF3DDC97),
    background = Bg,
    onBackground = Color(0xFFF2F3F7),
    surface = Surface,
    onSurface = Color(0xFFF2F3F7),
    surfaceVariant = SurfaceHi,
    onSurfaceVariant = Color(0xFF9AA1B0),
    outline = Color(0xFF2A2F3A)
)

private val LightColors = lightColorScheme(
    primary = Accent,
    secondary = Color(0xFF12A56C)
)

@Composable
fun MusicBoxTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
