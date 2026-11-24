package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.ReservationRow
import com.example.lottcamp.ui.components.AppChipRow
import com.example.lottcamp.ui.components.AppDialog
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.components.AppReservationCard
import com.example.lottcamp.ui.theme.LottCampTheme

@Composable
fun ReservationsScreen(
    filters: List<String>,
    selected: Int,
    onFilterSelect: (Int) -> Unit,
    rows: List<ReservationRow>,
    rejecting: ReservationRow?,
    rejectMessage: String,
    onMessage: (ReservationRow) -> Unit,
    onReject: (ReservationRow) -> Unit,
    onRejectConfirm: () -> Unit,
    onRejectDismiss: () -> Unit,
    error: String?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    error?.let {
        AlertDialog(
            onDismissRequest = onDismissError,
            confirmButton = { TextButton(onClick = onDismissError) { Text("OK") } },
            title = { Text("Something went wrong") },
            text = { Text(it) },
        )
    }

    if (rejecting != null) {
        AppDialog(
            title = "Reject reservation?",
            message = rejectMessage,
            dismissText = "Keep it",
            onDismiss = onRejectDismiss,
            confirmText = "Reject",
            onConfirm = onRejectConfirm,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = "Reservations",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(8.dp))

        AppChipRow(labels = filters, selected = selected, onSelect = onFilterSelect)

        Spacer(Modifier.height(16.dp))

        if (rows.isEmpty()) {
            AppMutedText("No reservations here yet.")
        }

        LazyColumn(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            items(rows, key = { it.id }) { row ->
                AppReservationCard(
                    name = row.name,
                    subtitle = row.subtitle,
                    status = row.status,
                    showActions = row.canAct,
                    onMessage = { onMessage(row) },
                    onReject = { onReject(row) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationsScreenPreview() {
    LottCampTheme {
        ReservationsScreen(
            filters = listOf("Active", "Past", "Cancelled"),
            selected = 0,
            onFilterSelect = {},
            rows = listOf(
                ReservationRow("1", "Alex Morgan", "Pine Ridge Camp · May 8 – May 10, 2027", "Confirmed · RES-2038", true),
                ReservationRow("2", "Sam Patel", "Bow Valley Retreat · Jun 12 – Jun 14, 2027", "Confirmed · RES-2041", true),
            ),
            rejecting = null,
            rejectMessage = "",
            onMessage = {},
            onReject = {},
            onRejectConfirm = {},
            onRejectDismiss = {},
            error = null,
            onDismissError = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}