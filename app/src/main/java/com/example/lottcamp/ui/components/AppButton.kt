package com.example.lottcamp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Style variants available to [AppButton].
 *
 * - [Primary] filled with the theme's primary color.
 * - [Secondary] outlined surface. Used for cancel and alt actions.
 * - [Danger] filled with the theme's error color. Used for destructive actions.
 */
enum class AppButtonVariant { Primary, Secondary, Danger }

/**
 * Button that applies the LottCamp theme's colors, shape, and padding.
 *
 * @param text Label shown inside the button.
 * @param onClick Invoked when the button is pressed.
 * @param modifier Applied to the button itself.
 * @param variant Visual style. See [AppButtonVariant].
 * @param enabled When false the button is muted and does not respond to input.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
) {
    val containerColor = when (variant) {
        AppButtonVariant.Primary -> MaterialTheme.colorScheme.primary
        AppButtonVariant.Secondary -> MaterialTheme.colorScheme.surface
        AppButtonVariant.Danger -> MaterialTheme.colorScheme.error
    }

    val contentColor = when (variant) {
        AppButtonVariant.Primary -> MaterialTheme.colorScheme.onPrimary
        AppButtonVariant.Secondary -> MaterialTheme.colorScheme.primary
        AppButtonVariant.Danger -> MaterialTheme.colorScheme.onError
    }

    val border = if (variant == AppButtonVariant.Secondary) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    } else {
        null
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}