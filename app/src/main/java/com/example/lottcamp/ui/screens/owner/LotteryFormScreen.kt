package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppCheckbox
import com.example.lottcamp.ui.components.AppDateField
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.components.AppTextField
import com.example.lottcamp.ui.components.AppTopBar
import com.example.lottcamp.ui.theme.LottCampTheme

/** Form used to create a lottery and to edit an existing one. */
@Composable
fun LotteryFormScreen(
    title: String,
    submitText: String,
    options: List<Campsite>,
    selectedIds: Set<String>,
    onCampsiteToggle: (String) -> Unit,
    registrationStart: Long?,
    onRegistrationStartChange: (Long) -> Unit,
    registrationEnd: Long?,
    onRegistrationEndChange: (Long) -> Unit,
    bookingStart: Long?,
    onBookingStartChange: (Long) -> Unit,
    bookingEnd: Long?,
    onBookingEndChange: (Long) -> Unit,
    winners: String,
    onWinnersChange: (String) -> Unit,
    waitlistLimit: String,
    onWaitlistLimitChange: (String) -> Unit,
    formError: String?,
    saving: Boolean,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = title, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "CAMPSITES IN THIS LOTTERY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            options.forEach { campsite ->
                AppCheckbox(
                    checked = campsite.id in selectedIds,
                    onCheckedChange = { onCampsiteToggle(campsite.id) },
                    label = campsite.name,
                )
                Spacer(Modifier.height(8.dp))
            }

            // TODO: US 02.04.08, lotteries shared with other owners. Needs a way to find their campsites.
            AppCheckbox(
                checked = false,
                onCheckedChange = {},
                label = "Add a campsite from another owner",
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppDateField("Registration opens", registrationStart, onRegistrationStartChange, Modifier.weight(1f))
                AppDateField("Registration closes", registrationEnd, onRegistrationEndChange, Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppDateField("Booking starts", bookingStart, onBookingStartChange, Modifier.weight(1f))
                AppDateField("Booking ends", bookingEnd, onBookingEndChange, Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = winners,
                    onValueChange = onWinnersChange,
                    label = "Number of winners",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = waitlistLimit,
                    onValueChange = onWaitlistLimitChange,
                    label = "Waitlist limit (optional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))

            AppMutedText("Winners are drawn automatically when registration closes.")
        }

        Column(modifier = Modifier.padding(16.dp)) {
            formError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(8.dp))
            }
            AppButton(
                text = if (saving) "Saving..." else submitText,
                onClick = onSubmit,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LotteryFormScreenPreview() {
    val pine = Campsite("1", "o", "Pine Ridge Camp", "Hinton, AB", maxOccupancy = 8, pricePerNight = 65.0)
    val bow = Campsite("2", "o", "Bow Valley Retreat", "Canmore, AB", maxOccupancy = 6, pricePerNight = 50.0)
    LottCampTheme {
        LotteryFormScreen(
            title = "Create lottery",
            submitText = "Publish lottery",
            options = listOf(pine, bow),
            selectedIds = setOf("1", "2"),
            onCampsiteToggle = {},
            registrationStart = 1807401600000, onRegistrationStartChange = {},
            registrationEnd = 1808006400000, onRegistrationEndChange = {},
            bookingStart = 1808179200000, onBookingStartChange = {},
            bookingEnd = 1808784000000, onBookingEndChange = {},
            winners = "5", onWinnersChange = {},
            waitlistLimit = "50", onWaitlistLimitChange = {},
            formError = null,
            saving = false,
            onSubmit = {},
            onBack = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}