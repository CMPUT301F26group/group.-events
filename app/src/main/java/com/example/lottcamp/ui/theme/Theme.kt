package com.example.lottcamp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = SurfaceLight,
    secondary = SecondaryLight,
    onSecondary = TextLight,
    tertiary = PrimaryLight,
    onTertiary = SurfaceLight,
    background = BgLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = BorderLight,
    onSurfaceVariant = MutedLight,
    error = DangerLight,
    onError = SurfaceLight,
    outline = BorderLight,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = BgDark,
    secondary = SecondaryDark,
    onSecondary = TextDark,
    tertiary = PrimaryDark,
    onTertiary = BgDark,
    background = BgDark,
    onBackground = TextDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = BorderDark,
    onSurfaceVariant = MutedDark,
    error = DangerDark,
    onError = BgDark,
    outline = BorderDark,
)

@Composable
fun LottCampTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}