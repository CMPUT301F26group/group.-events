package com.example.lottcamp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** Visual styles for [AppChip]. */
enum class AppChipVariant { Outlined, Secondary, Primary }

/**
 * Selectable chip
 *
 * Selection is controlled by the caller: the chip reports taps through [onClick]
 * and renders itself from [selected].
 *
 * @param text Label inside the chip.
 * @param selected Whether the chip is currently active.
 * @param onClick Invoked when the chip is tapped.
 * @param modifier Applied to the chip.
 * @param contentPadding Space around the label. Pass smaller values for a compact chip.
 * @param variant Visual style. Defaults to [AppChipVariant.Primary] when selected,
 * otherwise [AppChipVariant.Outlined].
 */
@Composable
fun AppChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    variant: AppChipVariant = if (selected) AppChipVariant.Primary else AppChipVariant.Outlined,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Checkbox,
            ),
        shape = CircleShape,
        color = when (variant) {
            AppChipVariant.Outlined -> colors.surface
            AppChipVariant.Secondary -> colors.secondary
            AppChipVariant.Primary -> colors.primary
        },
        contentColor = when (variant) {
            AppChipVariant.Outlined -> colors.onSurface
            AppChipVariant.Secondary -> colors.onSecondary
            AppChipVariant.Primary -> colors.onPrimary
        },
        border = if (variant == AppChipVariant.Outlined) {
            BorderStroke(1.dp, colors.outline)
        } else {
            null
        },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(contentPadding),
        )
    }
}