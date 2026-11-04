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

enum class AppButtonVariant { Primary, Secondary, Danger }

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