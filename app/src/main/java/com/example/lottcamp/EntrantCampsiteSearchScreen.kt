package com.example.lottcamp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.lottcamp.ui.theme.LottCampTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.ui.components.AppFilterTag
import com.example.lottcamp.ui.components.AppIconButton
import com.example.lottcamp.ui.components.AppListingCard
import com.example.lottcamp.ui.components.AppNavBar
import com.example.lottcamp.ui.components.AppNavItem
import com.example.lottcamp.ui.components.AppSearchBar
import kotlin.collections.listOf
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.LotteryRepository

@Composable
fun EntrantCampsiteSearchScreen(navController: NavController, modifier: Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var currentFilter by remember { mutableStateOf(CampsiteFilter())}
    var searchResults by remember { mutableStateOf(listOf<Campsite>()) }
    val campsiteRepo = CampsiteRepository()
    val lotteryRepo = LotteryRepository()

    LaunchedEffect(currentFilter) {
        searchResults = currentFilter.getMatches(campsiteRepo, lotteryRepo)
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
                onQueryChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
            )
            AppIconButton(
                icon = Icons.Filled.FilterList,
                contentDescription = "Filter for campsites based on location, capacity, price and more",
                onClick = {},
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
                        onClick = { currentFilter = currentFilter.copy(locationName = null) },
                    )
                }
            }
            currentFilter.name?.let { name ->
                item {
                    AppFilterTag(
                        text = name,
                        onClick = { currentFilter = currentFilter.copy(name = null) },
                    )
                }
            }
            if (currentFilter.startDate != null && currentFilter.endDate != null) {
                item {
                    AppFilterTag(
                        text = "${currentFilter.startDate}-${currentFilter.endDate}",
                        onClick = { currentFilter = currentFilter.copy(startDate = null, endDate = null) },
                    )
                }
            }
            currentFilter.minOccupancy?.let { guests ->
                item {
                    AppFilterTag(
                        text = "$guests guests",
                        onClick = { currentFilter = currentFilter.copy(minOccupancy = null) },
                    )
                }
            }
            currentFilter.maxPricePerNight?.let { price ->
                item {
                    AppFilterTag(
                        text = "\$$price",
                        onClick = { currentFilter = currentFilter.copy(maxPricePerNight = null) },
                    )
                }
            }
            items(currentFilter.tags) { tag ->
                AppFilterTag(
                    text = tag,
                    onClick = { currentFilter = currentFilter.copy(tags = currentFilter.tags - tag) },
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
                text = "${searchResults.size} results",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!searchResults.isEmpty()) {
                for (campsite in searchResults) {
                    item {
                        // TODO: Add campsite thumbnail
                        AppListingCard(
                            title = campsite.name,
                            location = campsite.locationName,
                            rating = campsite.averageRating.toFloat(),
                            price = "\$${campsite.pricePerNight}",
                            priceUnit = "/ Night",
                            onClick = {
                                navController.navigate("entrant-campsite-details-screen/${campsite.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EntrantCampsiteSearchScreenPreview() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    LottCampTheme {
        Scaffold(
            bottomBar = {
                AppNavBar {
                    AppNavItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        label = "Explore",
                        icon = Icons.Outlined.Home,
                    )
                    AppNavItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        label = "Bookings",
                        icon = Icons.Outlined.DateRange,
                    )
                    AppNavItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        label = "Messages",
                        icon = Icons.Outlined.Email,
                    )
                    AppNavItem(
                        selected = tab == 3,
                        onClick = { tab = 3 },
                        label = "Profile",
                        icon = Icons.Outlined.Person,
                    )
                }
            }
        ) { innerPadding ->
            Text("bingus", Modifier.padding(innerPadding))
        }
    }
}