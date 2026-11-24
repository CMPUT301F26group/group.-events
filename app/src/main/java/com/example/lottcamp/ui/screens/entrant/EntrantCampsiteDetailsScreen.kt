package com.example.lottcamp.ui.screens.entrant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.ImageRepository
import com.example.lottcamp.repository.LotteryRepository
import com.example.lottcamp.ui.components.AppBackButton
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppButtonVariant
import com.example.lottcamp.ui.components.AppRatingBar
import com.example.lottcamp.ui.components.AppStoredImage


@Composable
fun EntrantCampsiteDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    campsiteId: String,
) {
    val campsiteRepo = CampsiteRepository()
    val lotteryRepo = LotteryRepository()
    val imageRepo = ImageRepository()
    var campsite by remember { mutableStateOf<Campsite?>(null) }

    LaunchedEffect(campsiteId) {
        campsite = campsiteRepo.getCampsiteById(campsiteId)
    }

    val cs = campsite ?: return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppBackButton("Back to Search", onClick = { navController.navigate("entrant-campsite-search-screen") })
        }

        item {
            val shape = MaterialTheme.shapes.large
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape),
                contentAlignment = Alignment.Center,
            ) {
                AppStoredImage(
                    imageId = cs.imageId,
                    imageRepo = imageRepo,
                    contentDescription = cs.name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(cs.name, style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${cs.averageRating} ", style = MaterialTheme.typography.bodyMedium)
                    AppRatingBar(cs.averageRating.toFloat())
                    Text(" (${cs.ratingCount} reviews)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${cs.locationName} · Sleeps up to ${cs.maxOccupancy} · \$${cs.pricePerNight.toInt()} / night",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Text(cs.description, style = MaterialTheme.typography.bodyLarge)
        }

        // TODO: Need to add calendar component and logic for entering lottery for a certain window


        item {
            AppButton(
                "Join waiting list",
                onClick = { /* TODO: join waiting list */ },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            AppButton(
                "Rate this campsite",
                onClick = { navController.navigate("entrant-campsite-rating-screen/${cs.id}") },
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Secondary,
            )
        }
    }
}