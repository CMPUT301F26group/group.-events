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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.ImageRepository
import com.example.lottcamp.repository.LotteryRepository
import com.example.lottcamp.ui.components.AppFilterTag
import com.example.lottcamp.ui.components.AppIconButton
import com.example.lottcamp.ui.components.AppListingCard
import com.example.lottcamp.ui.components.AppSearchBar
import com.example.lottcamp.ui.components.AppStoredImage
import java.util.Locale

/**
 * Displays the entrant campsite search and discovery screen.
 *
 * Fetches campsites matching the active [CampsiteFilter] from Firestore, performs real-time
 * case-insensitive substring filtering against campsite names and locations, displays active
 * filter tags, and navigates to campsite details, filters, or QR scanning.
 *
 * @param navController Navigation controller for opening filter, QR scan, and campsite details screens.
 * @param modifier Modifier applied to the root layout container.
 * @param viewModel Shared [SearchViewModel] retaining the active search text and filter state.
 */
@Composable
fun EntrantCampsiteSearchScreen(
    navController: NavController,
    modifier: Modifier,
    viewModel: SearchViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    var baseResults by remember { mutableStateOf(listOf<Campsite>()) }

    val campsiteRepo = remember { CampsiteRepository() }
    val lotteryRepo = remember { LotteryRepository() }
    val imageRepo = remember { ImageRepository() }

    LaunchedEffect(currentFilter) {
        baseResults = currentFilter.getMatches(campsiteRepo, lotteryRepo)
    }

    val displayedResults = remember(baseResults, searchQuery) {
        val normalizedTerm = SearchViewModel
            .sanitizeSearchInput(searchQuery)
            .trim()
            .lowercase(Locale.getDefault())

        if (normalizedTerm.isEmpty()) {
            baseResults
        } else {
            baseResults.filter { campsite ->
                campsite.name.lowercase(Locale.getDefault()).contains(normalizedTerm) ||
                        campsite.locationName.lowercase(Locale.getDefault()).contains(normalizedTerm)
            }
        }
    }

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
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.weight(1f),
            )
            AppIconButton(
                icon = Icons.Filled.FilterList,
                contentDescription = "Filter for campsites based on location, capacity, price and more",
                onClick = {
                    navController.navigate("entrant-campsite-filter-screen")
                },
            )
            AppIconButton(
                icon = Icons.Filled.QrCode2,
                contentDescription = "Find a campsite from promotional QR code",
                onClick = {},
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
                        onClick = { viewModel.setFilter(currentFilter.copy(locationName = null)) },
                    )
                }
            }
            currentFilter.name?.let { name ->
                item {
                    AppFilterTag(
                        text = name,
                        onClick = { viewModel.setFilter(currentFilter.copy(name = null)) },
                    )
                }
            }
            if (currentFilter.startDate != null && currentFilter.endDate != null) {
                item {
                    AppFilterTag(
                        text = "${currentFilter.startDate}-${currentFilter.endDate}",
                        onClick = { viewModel.setFilter(currentFilter.copy(startDate = null, endDate = null)) },
                    )
                }
            }
            currentFilter.minOccupancy?.let { guests ->
                item {
                    AppFilterTag(
                        text = "$guests guests",
                        onClick = { viewModel.setFilter(currentFilter.copy(minOccupancy = null)) },
                    )
                }
            }
            currentFilter.maxPricePerNight?.let { price ->
                item {
                    AppFilterTag(
                        text = "\$$price",
                        onClick = { viewModel.setFilter(currentFilter.copy(maxPricePerNight = null)) },
                    )
                }
            }
            items(currentFilter.tags) { tag ->
                AppFilterTag(
                    text = tag,
                    onClick = { viewModel.setFilter(currentFilter.copy(tags = currentFilter.tags - tag)) },
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
                    text = "No campsites match your search.",
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
                        onClick = {
                            navController.navigate("entrant-campsite-details-screen/${campsite.id}")
                        },
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