package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.WaitingRow
import com.example.lottcamp.ui.components.AppAvatar
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppDivider
import com.example.lottcamp.ui.components.AppListRow
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.components.AppTopBar
import com.example.lottcamp.ui.theme.LottCampTheme

@Composable
fun WaitingListScreen(
    campsiteNames: String,
    summary: String,
    drawText: String,
    rows: List<WaitingRow>,
    error: String?,
    onDismissError: () -> Unit,
    onEdit: () -> Unit,
    onSendNotification: () -> Unit,
    onBack: () -> Unit,
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

    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = "Waiting list", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            // Tapping the summary opens Edit lottery
            Surface(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = campsiteNames,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    AppMutedText(summary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = drawText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(rows, key = { it.id }) { row ->
                    AppListRow(
                        title = row.name,
                        subtitle = row.subtitle,
                        trailing = row.status,
                        leading = { AppAvatar(name = row.name) },
                    )
                    AppDivider(spacing = 8.dp)
                }
            }
        }

        AppButton(
            text = "Send notification",
            onClick = onSendNotification,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WaitingListScreenPreview() {
    LottCampTheme {
        WaitingListScreen(
            campsiteNames = "Pine Ridge Camp",
            summary = "18 entrants · Closes Apr 20, 2027",
            drawText = "5 winners drawn automatically",
            rows = listOf(
                WaitingRow("1", "Alex Morgan", "Joined Apr 14 · alex.morgan@example.com", ""),
                WaitingRow("2", "Sam Patel", "Joined Apr 14 · sam.patel@example.com", ""),
                WaitingRow("3", "Robin Lee", "Joined Apr 15 · robin.lee@example.com", ""),
            ),
            error = null,
            onDismissError = {},
            onEdit = {},
            onSendNotification = {},
            onBack = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}