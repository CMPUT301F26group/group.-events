package com.example.lottcamp.ui.screens.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.EntrantSearchController
import com.example.lottcamp.ui.components.AppBackButton
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppButtonVariant

object EntrantFilterTestTags {
    const val BACK_BUTTON = "filter_back_button"
    const val TITLE_TEXT = "filter_title_text"
    const val LOCATION_INPUT = "filter_location_input"
    const val MIN_GUESTS_INPUT = "filter_min_guests_input"
    const val MAX_PRICE_INPUT = "filter_max_price_input"
    const val CLEAR_BUTTON = "filter_clear_button"
    const val APPLY_BUTTON = "filter_apply_button"
}

/**
 * Filter screen allowing an entrant to refine campsite search criteria through
 * [EntrantSearchController].
 *
 * @param controller Shared search controller holding and applying filter state.
 * @param modifier Modifier applied to the root container.
 */
@Composable
fun EntrantCampsiteFilterScreen(
    controller: EntrantSearchController,
    modifier: Modifier = Modifier,
) {
    val current = controller.filter
    var locationText by remember(current.locationName) { mutableStateOf(current.locationName ?: "") }
    var guestsText by remember(current.minOccupancy) { mutableStateOf(current.minOccupancy?.toString() ?: "") }
    var maxPriceText by remember(current.maxPricePerNight) {
        mutableStateOf(current.maxPricePerNight?.toString() ?: "")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppBackButton(
            label = "Back",
            onClick = controller::onBackClick,
            modifier = Modifier.testTag(EntrantFilterTestTags.BACK_BUTTON),
        )

        Text(
            text = "Filter Campsites",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag(EntrantFilterTestTags.TITLE_TEXT),
        )

        OutlinedTextField(
            value = locationText,
            onValueChange = { locationText = it },
            label = { Text("Location") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EntrantFilterTestTags.LOCATION_INPUT),
        )

        OutlinedTextField(
            value = guestsText,
            onValueChange = { guestsText = it },
            label = { Text("Minimum guests") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EntrantFilterTestTags.MIN_GUESTS_INPUT),
        )

        OutlinedTextField(
            value = maxPriceText,
            onValueChange = { maxPriceText = it },
            label = { Text("Max price per night ($)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EntrantFilterTestTags.MAX_PRICE_INPUT),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppButton(
                text = "Clear",
                onClick = {
                    locationText = ""
                    guestsText = ""
                    maxPriceText = ""
                    controller.clearFilters()
                    controller.onBackClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag(EntrantFilterTestTags.CLEAR_BUTTON),
                variant = AppButtonVariant.Secondary,
            )

            AppButton(
                text = "Apply",
                onClick = {
                    controller.updateFilter(
                        current.copy(
                            locationName = locationText.trim().ifEmpty { null },
                            minOccupancy = guestsText.trim().toIntOrNull()?.takeIf { it > 0 },
                            maxPricePerNight = maxPriceText.trim().toDoubleOrNull()?.takeIf { it >= 0.0 },
                        )
                    )
                    controller.onBackClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag(EntrantFilterTestTags.APPLY_BUTTON),
            )
        }
    }
}