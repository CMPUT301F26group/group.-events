package com.example.lottcamp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Card for one reservation: name, campsite and dates, a status line, and optional actions.
 *
 * @param name Entrant name.
 * @param subtitle Campsite and dates.
 * @param status Status line, e.g. "Confirmed · RES-2038".
 * @param showActions Whether the Message and Reject buttons are shown.
 * @param onMessage Invoked when Message is tapped.
 * @param onReject Invoked when Reject is tapped.
 * @param modifier Applied to the card.
 */
@Composable
fun AppReservationCard(
    name: String,
    subtitle: String,
    status: String,
    showActions: Boolean,
    onMessage: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            AppMutedText(subtitle)
            Spacer(Modifier.height(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (showActions) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppButton(
                        text = "Message",
                        onClick = onMessage,
                        variant = AppButtonVariant.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                    AppButton(
                        text = "Reject",
                        onClick = onReject,
                        variant = AppButtonVariant.Danger,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}