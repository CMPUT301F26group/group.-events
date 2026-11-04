package com.example.lottcamp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Bottom navigation bar for [AppNavItem] entries.
 *
 * Draws a divider above the bar and disables tonal elevation, since the surface
 * color already separates the bar from the content.
 *
 * @param modifier Applied to the wrapper column.
 * @param content Navigation items, normally [AppNavItem] calls.
 */
@Composable
fun AppNavBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline,
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            content = content
        )
    }
}

/**
 * One destination inside an [AppNavBar].
 *
 * Selected items get a primary pill behind the icon and a bold label.
 *
 * @param selected Whether this is the current destination.
 * @param onClick Invoked when the item is tapped.
 * @param label Text under the icon.
 * @param icon Icon shown above the label.
 */
@Composable
fun RowScope.AppNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}