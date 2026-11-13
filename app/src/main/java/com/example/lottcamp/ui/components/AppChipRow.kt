package com.example.lottcamp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Horizontally scrolling row of compact [AppChip]s where exactly one is selected,
 * e.g. "Active / Past / Waiting lists" filters.
 *
 * @param labels Chip labels.
 * @param selected Index of the selected chip in [labels].
 * @param onSelect Called with the index of the tapped chip.
 * @param modifier Applied to the row.
 */
@Composable
fun AppChipRow(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            AppChip(
                text = label,
                selected = index == selected,
                onClick = { onSelect(index) },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}
