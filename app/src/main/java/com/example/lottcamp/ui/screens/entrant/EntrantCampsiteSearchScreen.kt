package com.example.lottcamp.ui.screens.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.EntrantSearchController
import com.example.lottcamp.repository.ImageRepository
import com.example.lottcamp.ui.components.AppFilterTag
import com.example.lottcamp.ui.components.AppIconButton
import com.example.lottcamp.ui.components.AppListingCard
import com.example.lottcamp.ui.components.AppSearchBar
import com.example.lottcamp.ui.components.AppStoredImage

/**
 * Displays the entrant campsite search screen backed by [EntrantSearchController].
 *
 * @param controller Controller managing search query, filters, and matching campsites.
 * @param modifier Modifier applied to the root column.
 */
@Composable
fun EntrantCampsiteSearchScreen(
    controller: EntrantSearchController,
    modifier: Modifier = Modifier,
) {
    val imageRepo = remember { ImageRepository() }
    val currentFilter = controller.filter
    val displayedResults = controller.displayedResults

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppSearchBar(
                query = controller.searchQuery,
                onQueryChange = controller::onSearchQueryChange,
                modifier = Modifier.weight(1f),
            )
            AppIconButton(
                icon = Icons.Filled.FilterList,
                contentDescription = "Filter for campsites based on location, capacity, price and more",
                onClick = controller::onOpenFilterClick,
            )
            AppIconButton(
                icon = Icons.Filled.QrCode2,
                contentDescription = "Find a campsite from promotional QR code",
                onClick = controller::onQrScanClick,
            )
        }

        LazyRow(
            modifier = Modifier.heightIn(min = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currentFilter.locationName?.let { name ->
                item {
                    AppFilterTag(
                        text = name,
                        onClick = controller::removeLocationFilter,
                    )
                }
            }
            currentFilter.name?.let { name ->
                item {
                    AppFilterTag(
                        text = name,
                        onClick = controller::removeNameFilter,
                    )
                }
            }
            if (currentFilter.startDate != null && currentFilter.endDate != null) {
                item {
                    AppFilterTag(
                        text = "${currentFilter.startDate}-${currentFilter.endDate}",
                        onClick = controller::removeDateRangeFilter,
                    )
                }
            }
            currentFilter.minOccupancy?.let { guests ->
                item {
                    AppFilterTag(
                        text = "$guests guests",
                        onClick = controller::removeOccupancyFilter,
                    )
                }
            }
            currentFilter.maxPricePerNight?.let { price ->
                item {
                    AppFilterTag(
                        text = "\$$price",
                        onClick = controller::removeMaxPriceFilter,
                    )
                }
            }
            items(currentFilter.tags) { tag ->
                AppFilterTag(
                    text = tag,
                    onClick = { controller.removeTagFilter(tag) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Campsites",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${displayedResults.size} results",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (displayedResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (controller.isLoading) "Loading campsites..." else "No campsites match your search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(displayedResults, key = { it.id }) { campsite ->
                    AppListingCard(
                        title = campsite.name,
                        location = campsite.locationName,
                        rating = campsite.averageRating.toFloat(),
                        price = "\$${campsite.pricePerNight}",
                        priceUnit = "/ Night",
                        onClick = { controller.onCampsiteClick(campsite.id) },
                        image = {
                            AppStoredImage(
                                campsite.imageId,
                                imageRepo,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}