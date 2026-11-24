package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppButtonVariant
import com.example.lottcamp.ui.components.AppCalendar
import com.example.lottcamp.ui.components.AppDivider
import com.example.lottcamp.ui.components.AppInfoRow
import com.example.lottcamp.ui.components.AppMapBox
import com.example.lottcamp.ui.components.AppPhotoBox
import com.example.lottcamp.ui.components.AppTopBar
import com.example.lottcamp.ui.theme.LottCampTheme

@Composable
fun CampsiteDetailsScreen(
    name: String,
    imageId: String?,
    image: @Composable (String) -> Unit,
    location: String,
    capacityAndPrice: String,
    year: Int,
    month: Int,
    bookedDays: Set<Int>,
    lotteryText: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDownloadQr: () -> Unit,
    onEdit: () -> Unit,
    onCreateLottery: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = name, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppPhotoBox(imageId = imageId, image = image, modifier = Modifier.weight(1f))
                AppMapBox(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            AppInfoRow("Location", location)
            AppDivider(spacing = 8.dp)
            AppInfoRow("Capacity & price", capacityAndPrice)

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Promotional QR code",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(8.dp))
                        AppButton(
                            text = "Download QR",
                            onClick = onDownloadQr,
                            variant = AppButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "BOOKED DATES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            AppCalendar(
                year = year,
                month = month,
                bookedDays = bookedDays,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppButton(
                    text = "Edit campsite",
                    onClick = onEdit,
                    variant = AppButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = lotteryText,
                    onClick = onCreateLottery,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampsiteDetailsScreenPreview() {
    LottCampTheme {
        CampsiteDetailsScreen(
            name = "Pine Ridge Camp",
            imageId = null,
            image = {},
            location = "Hinton, AB",
            capacityAndPrice = "8 guests · \$65 / night",
            year = 2027,
            month = 5,
            bookedDays = setOf(8, 9, 22, 23),
            lotteryText = "Create lottery",
            onPreviousMonth = {},
            onNextMonth = {},
            onDownloadQr = {},
            onEdit = {},
            onCreateLottery = {},
            onBack = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}