package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.SentNotification
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppDivider
import com.example.lottcamp.ui.components.AppListRow
import com.example.lottcamp.ui.components.AppSectionTitle
import com.example.lottcamp.ui.components.AppTextField
import com.example.lottcamp.ui.components.AppTopBar
import com.example.lottcamp.ui.theme.LottCampTheme

@Composable
fun OwnerNotificationsScreen(
    message: String,
    onMessageChange: (String) -> Unit,
    recipientCount: Int,
    sending: Boolean,
    sent: List<SentNotification>,
    error: String?,
    onDismissError: () -> Unit,
    onSend: () -> Unit,
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
        AppTopBar(title = "Notifications", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // TODO: the design shows a taller, multi-line box. AppTextField is single-line only.
            AppTextField(
                value = message,
                onValueChange = onMessageChange,
                label = "Message to waiting list ($recipientCount)",
                placeholder = "Write a message",
            )

            Spacer(Modifier.height(8.dp))

            AppButton(
                text = if (sending) "Sending..." else "Send notification",
                onClick = onSend,
                enabled = !sending,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            AppSectionTitle("Sent notifications")

            Spacer(Modifier.height(8.dp))

            sent.forEach { item ->
                AppListRow(
                    title = item.title,
                    subtitle = item.subtitle,
                    trailing = item.date,
                    leading = {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                AppDivider(spacing = 8.dp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerNotificationsScreenPreview() {
    LottCampTheme {
        OwnerNotificationsScreen(
            message = "Reminder: the lottery closes Apr 20.",
            onMessageChange = {},
            recipientCount = 18,
            sending = false,
            sent = listOf(
                SentNotification("Lottery reminder", "18 entrants", "Today"),
                SentNotification("Lottery drawn", "5 entrants", "Apr 6"),
            ),
            error = null,
            onDismissError = {},
            onSend = {},
            onBack = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}