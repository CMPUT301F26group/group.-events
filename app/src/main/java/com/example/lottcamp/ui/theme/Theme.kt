package com.example.lottcamp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private fun buildLightScheme(
    primary: Color,
    secondary: Color,
    background: Color,
    surface: Color,
    text: Color,
    muted: Color,
    border: Color,
    danger: Color,
    surfaceVariant: Color = border,
) = lightColorScheme(
    primary = primary,
    onPrimary = surface,
    secondary = secondary,
    onSecondary = text,
    tertiary = primary,
    onTertiary = surface,
    background = background,
    onBackground = text,
    surface = surface,
    onSurface = text,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = muted,
    error = danger,
    onError = surface,
    outline = border,
)

private fun buildDarkScheme(
    primary: Color,
    secondary: Color,
    background: Color,
    surface: Color,
    text: Color,
    muted: Color,
    border: Color,
    danger: Color,
    surfaceVariant: Color = border,
) = darkColorScheme(
    primary = primary,
    onPrimary = background,
    secondary = secondary,
    onSecondary = text,
    tertiary = primary,
    onTertiary = background,
    background = background,
    onBackground = text,
    surface = surface,
    onSurface = text,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = muted,
    error = danger,
    onError = background,
    outline = border,
)

private val LightColors = buildLightScheme(
    PrimaryLight, SecondaryLight, BgLight, SurfaceLight, TextLight, MutedLight, BorderLight, DangerLight,
)
private val DarkColors = buildDarkScheme(
    PrimaryDark, SecondaryDark, BgDark, SurfaceDark, TextDark, MutedDark, BorderDark, DangerDark,
)

private val RgLightColors = buildLightScheme(
    RgPrimaryLight, RgSecondaryLight, RgBgLight, RgSurfaceLight, RgTextLight, RgMutedLight, RgBorderLight, RgDangerLight,
)
private val RgDarkColors = buildDarkScheme(
    RgPrimaryDark, RgSecondaryDark, RgBgDark, RgSurfaceDark, RgTextDark, RgMutedDark, RgBorderDark, RgDangerDark,
)

private val ByLightColors = buildLightScheme(
    ByPrimaryLight, BySecondaryLight, ByBgLight, BySurfaceLight, ByTextLight, ByMutedLight, ByBorderLight, ByDangerLight,
)
private val ByDarkColors = buildDarkScheme(
    ByPrimaryDark, BySecondaryDark, ByBgDark, BySurfaceDark, ByTextDark, ByMutedDark, ByBorderDark, ByDangerDark,
)

private val HcLightColors = buildLightScheme(
    HcPrimaryLight, HcSecondaryLight, HcBgLight, HcSurfaceLight, HcTextLight, HcMutedLight, HcBorderLight, HcDangerLight,
    surfaceVariant = HcSurfaceVariantLight,
)
private val HcDarkColors = buildDarkScheme(
    HcPrimaryDark, HcSecondaryDark, HcBgDark, HcSurfaceDark, HcTextDark, HcMutedDark, HcBorderDark, HcDangerDark,
    surfaceVariant = HcSurfaceVariantDark,
)

/** Selects the light or dark [ColorScheme] matching [colorBlindMode]. */
private fun colorSchemeFor(darkTheme: Boolean, colorBlindMode: ColorBlindMode): ColorScheme =
    when (colorBlindMode) {
        ColorBlindMode.OFF -> if (darkTheme) DarkColors else LightColors
        ColorBlindMode.DEUTERANOPIA -> if (darkTheme) RgDarkColors else RgLightColors
        ColorBlindMode.TRITANOPIA -> if (darkTheme) ByDarkColors else ByLightColors
        ColorBlindMode.HIGH_CONTRAST -> if (darkTheme) HcDarkColors else HcLightColors
    }

/**
 * Root theme for LottCamp, applying light/dark colors and [accessibilityController] preferences.
 *
 * @param darkTheme Whether to use dark color palettes.
 * @param accessibilityController Active accessibility settings for color palette and UI/text scaling.
 * @param content Composable hierarchy rendered inside the theme.
 */
@Composable
fun LottCampTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accessibilityController: AccessibilityController = remember { AccessibilityController() },
    content: @Composable () -> Unit,
) {
    val settings = accessibilityController.settings
    val baseDensity = LocalDensity.current
    val uiScale = settings.uiSize.uiScale
    val fontScale = settings.textSize.fontScale
    // Scale dp by uiScale while keeping sp scaled independently by fontScale.
    val adjustedDensity = Density(
        density = baseDensity.density * uiScale,
        fontScale = baseDensity.fontScale * (fontScale / uiScale),
    )

    CompositionLocalProvider(
        LocalAccessibilityController provides accessibilityController,
        LocalDensity provides adjustedDensity,
    ) {
        MaterialTheme(
            colorScheme = colorSchemeFor(darkTheme, settings.colorBlindMode),
            typography = Typography,
            content = content,
        )
    }
}