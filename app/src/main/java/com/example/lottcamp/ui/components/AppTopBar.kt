package com.example.lottcamp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lottcamp.ui.theme.ScaledButtonControl

/**
 * Top bar with a back arrow and a title, with a divider underneath.
 * Used by every sub screen that can go back.
 *
 * @param title Screen title.
 * @param onBack Invoked when the back arrow is tapped.
 * @param modifier Modifier applied to the top bar container.
 * @param backButtonModifier Modifier applied to the back arrow [IconButton].
 * @param titleModifier Modifier applied to the title [Text].
 */
@Composable
fun AppTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backButtonModifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScaledButtonControl {
                IconButton(onClick = onBack, modifier = backButtonModifier) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = titleModifier)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}
