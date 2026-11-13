package com.example.lottcamp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Row with a leading graphic, a title and subtitle, and optional trailing text.
 *
 * @param title Main text.
 * @param subtitle Muted text under the title.
 * @param onClick Makes the row clickable and shows an arrow. Null keeps it static.
 * @param trailing Muted text before the arrow, e.g. a role or a date.
 * @param leading Graphic at the start of the row, e.g. [AppAvatar] or [AppImagePlaceholder].
 */
@Composable
fun AppListRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: String = "",
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            AppMutedText(subtitle)
        }
        AppMutedText(trailing)
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Divider between list rows. [spacing] is the gap above and below the line. */
@Composable
fun AppDivider(spacing: Dp) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = spacing),
        color = MaterialTheme.colorScheme.outline,
    )
}
