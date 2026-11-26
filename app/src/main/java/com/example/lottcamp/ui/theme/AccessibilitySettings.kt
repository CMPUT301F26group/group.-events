package com.example.lottcamp.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.edit

/** Color vision palettes available in accessibility mode. */
enum class ColorBlindMode(val label: String) {
    OFF("Off"),
    DEUTERANOPIA("Red-Green"),
    TRITANOPIA("Blue-Yellow"),
    HIGH_CONTRAST("High contrast"),
}

/**
 * Size presets used for scalable text, buttons, and UI components.
 *
 * @param label Short label shown in the accessibility quick settings.
 * @param fontScale Multiplier applied to text sizes.
 * @param buttonScale Multiplier applied to button sizing and padding.
 * @param uiScale Multiplier applied to layout density for general UI components.
 */
enum class AccessibilitySize(
    val label: String,
    val fontScale: Float,
    val buttonScale: Float,
    val uiScale: Float,
) {
    SMALL("Small", fontScale = 0.85f, buttonScale = 0.85f, uiScale = 0.90f),
    NORMAL("Normal", fontScale = 1.0f, buttonScale = 1.0f, uiScale = 1.0f),
    LARGE("Large", fontScale = 1.2f, buttonScale = 1.25f, uiScale = 1.12f),
    EXTRA_LARGE("Extra large", fontScale = 1.35f, buttonScale = 1.45f, uiScale = 1.22f),
}

/**
 * User-selected accessibility preferences applied app-wide through [LottCampTheme].
 *
 * @param colorBlindMode Active color palette for color vision deficiency or high contrast.
 * @param textSize Scale preset for all text across the app.
 * @param buttonSize Scale preset for buttons and icon buttons.
 * @param uiSize Scale preset for general UI component sizing.
 */
data class AccessibilitySettings(
    val colorBlindMode: ColorBlindMode = ColorBlindMode.OFF,
    val textSize: AccessibilitySize = AccessibilitySize.NORMAL,
    val buttonSize: AccessibilitySize = AccessibilitySize.NORMAL,
    val uiSize: AccessibilitySize = AccessibilitySize.NORMAL,
) {
    /** True when every accessibility option is set to its default value. */
    val isDefault: Boolean
        get() = this == AccessibilitySettings()
}

/**
 * Holds the active [AccessibilitySettings] and forwards changes to optional persistence.
 *
 * @param initial Starting settings when the controller is created.
 * @param onSave Invoked whenever settings change.
 */
class AccessibilityController(
    initial: AccessibilitySettings = AccessibilitySettings(),
    private val onSave: (AccessibilitySettings) -> Unit = {},
) {
    var settings by mutableStateOf(initial)
        private set

    /** Replaces the active settings and triggers [onSave]. */
    fun update(newSettings: AccessibilitySettings) {
        settings = newSettings
        onSave(newSettings)
    }

    /** Restores all accessibility settings to their defaults. */
    fun reset() = update(AccessibilitySettings())
}

/** CompositionLocal providing the active [AccessibilityController] to any screen or component. */
val LocalAccessibilityController = compositionLocalOf { AccessibilityController() }

/**
 * Scales interactive controls (such as Material switches and icon buttons) by the active
 * button size multiplier while keeping text scaling independent.
 */
@Composable
fun ScaledButtonControl(content: @Composable () -> Unit) {
    val buttonScale = LocalAccessibilityController.current.settings.buttonSize.buttonScale
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density.density * buttonScale,
            fontScale = density.fontScale / buttonScale,
        ),
        content = content,
    )
}

private const val PREFS_NAME = "lottcamp_accessibility"
private const val KEY_COLOR_BLIND = "color_blind_mode"
private const val KEY_TEXT_SIZE = "text_size"
private const val KEY_BUTTON_SIZE = "button_size"
private const val KEY_UI_SIZE = "ui_size"

/** Loads saved [AccessibilitySettings] from local device preferences. */
fun loadAccessibilitySettings(context: Context): AccessibilitySettings {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return AccessibilitySettings(
        colorBlindMode = enumOrDefault(prefs.getString(KEY_COLOR_BLIND, null), ColorBlindMode.OFF),
        textSize = enumOrDefault(prefs.getString(KEY_TEXT_SIZE, null), AccessibilitySize.NORMAL),
        buttonSize = enumOrDefault(prefs.getString(KEY_BUTTON_SIZE, null), AccessibilitySize.NORMAL),
        uiSize = enumOrDefault(prefs.getString(KEY_UI_SIZE, null), AccessibilitySize.NORMAL),
    )
}

/** Saves [settings] to local device preferences so they persist across app restarts. */
fun saveAccessibilitySettings(context: Context, settings: AccessibilitySettings) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(KEY_COLOR_BLIND, settings.colorBlindMode.name)
        putString(KEY_TEXT_SIZE, settings.textSize.name)
        putString(KEY_BUTTON_SIZE, settings.buttonSize.name)
        putString(KEY_UI_SIZE, settings.uiSize.name)
    }
}

private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
    name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
