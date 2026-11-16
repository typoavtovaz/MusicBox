package com.margy.musicbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Nothing-стиль: чёрный, белый, один красный акцент. Никакой радуги. */
val NothingRed = Color(0xFFD71921)
val NothingWhite = Color(0xFFF5F5F5)
val NothingBlack = Color(0xFF000000)
val NothingSurface = Color(0xFF0D0D0D)
val NothingSurfaceHi = Color(0xFF161616)
val NothingLine = Color(0xFF2A2A2A)
val NothingDim = Color(0xFF7A7A7A)

private val DarkColors = darkColorScheme(
    primary = NothingRed,
    onPrimary = NothingWhite,
    primaryContainer = NothingRed,
    onPrimaryContainer = NothingWhite,
    secondary = NothingWhite,
    onSecondary = NothingBlack,
    background = NothingBlack,
    onBackground = NothingWhite,
    surface = NothingSurface,
    onSurface = NothingWhite,
    surfaceVariant = NothingSurfaceHi,
    onSurfaceVariant = NothingDim,
    outline = NothingLine,
    error = NothingRed
)

private val LightColors = lightColorScheme(
    primary = NothingRed,
    background = Color(0xFFF7F7F7),
    surface = Color.White
)

private val base = Typography()

/** Моноширинный шрифт вместо дота-матричного Ndot — без внешних файлов, но вайб тот же. */
val NothingTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
    displayMedium = base.displayMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
    headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
    titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
    titleSmall = base.titleSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
    bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    bodySmall = base.bodySmall.copy(fontFamily = FontFamily.SansSerif),
    labelLarge = base.labelLarge.copy(fontFamily = FontFamily.Monospace, letterSpacing = 1.sp),
    labelMedium = base.labelMedium.copy(fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 1.2.sp)
)

@Composable
fun MusicBoxTheme(
    dark: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = NothingTypography,
        content = content
    )
}
