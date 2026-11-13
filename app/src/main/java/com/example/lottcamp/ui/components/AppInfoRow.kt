package com.example.lottcamp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Muted label on the left and its value on the right, e.g. "Dates    May 8 - May 10".
 *
 * @param label Name of the value.
 * @param value Value shown at the end of the row.
 */
@Composable
fun AppInfoRow(label: String, value: String) {
    Row {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value)
    }
}
