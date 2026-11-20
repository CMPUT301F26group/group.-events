package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppChip
import com.example.lottcamp.ui.components.AppMapBox
import com.example.lottcamp.ui.components.AppPhotoBox
import com.example.lottcamp.ui.components.AppTextField
import com.example.lottcamp.ui.components.AppTopBar
import com.example.lottcamp.ui.theme.LottCampTheme

/** Form used to register a new campsite and to edit an existing one. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CampsiteFormScreen(
    title: String,
    submitText: String,
    imageUrl: String?,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    location: String,
    onLocationChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    maxOccupancy: String,
    onMaxOccupancyChange: (String) -> Unit,
    occupancyError: String?,
    pricePerNight: String,
    onPricePerNightChange: (String) -> Unit,
    priceError: String?,
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagClick: (String) -> Unit,
    onPhotoClick: () -> Unit,
    onMapClick: () -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppPhotoBox(imageUrl = imageUrl, onClick = onPhotoClick, modifier = Modifier.weight(1f))
                AppMapBox(onClick = onMapClick, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            AppTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Campsite name *",
                placeholder = "e.g. Lake View Camp",
                supportingText = nameError,
                isError = nameError != null,
            )

            Spacer(Modifier.height(8.dp))

            AppTextField(
                value = location,
                onValueChange = onLocationChange,
                label = "Location",
                placeholder = "e.g. Canmore, Alberta",
            )

            Spacer(Modifier.height(8.dp))

            AppTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Description & amenities",
                placeholder = "Describe your campsite",
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppTextField(
                    value = maxOccupancy,
                    onValueChange = onMaxOccupancyChange,
                    label = "Max occupancy",
                    placeholder = "e.g. 6",
                    supportingText = occupancyError,
                    isError = occupancyError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = pricePerNight,
                    onValueChange = onPricePerNightChange,
                    label = "Price / night",
                    placeholder = "e.g. \$65",
                    supportingText = priceError,
                    isError = priceError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "TAGS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableTags.forEach { tag ->
                    AppChip(
                        text = tag,
                        selected = tag in selectedTags,
                        onClick = { onTagClick(tag) },
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            AppButton(
                text = submitText,
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampsiteFormScreenPreview() {
    LottCampTheme {
        CampsiteFormScreen(
            title = "Edit campsite",
            submitText = "Save changes",
            imageUrl = null,
            name = "Pine Ridge Camp",
            onNameChange = {},
            nameError = null,
            location = "Hinton, AB",
            onLocationChange = {},
            description = "Riverside sites, cookhouse, tennis court",
            onDescriptionChange = {},
            maxOccupancy = "8",
            onMaxOccupancyChange = {},
            occupancyError = null,
            pricePerNight = "\$65",
            onPricePerNightChange = {},
            priceError = null,
            availableTags = listOf("Fire pit", "Cookhouse", "Tennis"),
            selectedTags = setOf("Fire pit", "Cookhouse", "Tennis"),
            onTagClick = {},
            onPhotoClick = {},
            onMapClick = {},
            onBack = {},
            onSubmit = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}