package com.example.lottcamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.lottcamp.ui.theme.SurfaceLight
import com.example.lottcamp.ui.theme.TextLight

/**
 * Square icon-only button.
 *
 * Used next to the search bar for actions like filters and QR scanning.
 *
 * @param icon Icon shown in the center of the button.
 * @param contentDescription Accessibility label describing the action.
 * @param onClick Invoked when the button is pressed.
 * @param modifier Applied to the button itself.
 * @param enabled When false the button is muted and does not respond to input.
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val containerColor = SurfaceLight
    val contentColor = TextLight
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
    }
}