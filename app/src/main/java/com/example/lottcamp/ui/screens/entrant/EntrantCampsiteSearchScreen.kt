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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.EntrantSearchController
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.ImageRepository
import com.example.lottcamp.ui.components.AppFilterTag
import com.example.lottcamp.ui.components.AppHeader
import com.example.lottcamp.ui.components.AppIconButton
import com.example.lottcamp.ui.components.AppListingCard
import com.example.lottcamp.ui.components.AppSearchBar
import com.example.lottcamp.ui.components.AppStoredImage

object EntrantSearchTestTags {
    const val SEARCH_BAR = "search_bar"
    const val FILTER_BUTTON = "search_filter_button"
    const val QR_SCAN_BUTTON = "search_qr_scan_button"
    const val ACTIVE_FILTERS_ROW = "search_active_filters_row"
    const val RESULTS_COUNT_TEXT = "search_results_count_text"
    const val EMPTY_STATE_TEXT = "search_empty_state_text"
    const val RESULTS_LIST = "search_results_list"

    fun activeFilterTag(key: String) = "search_active_filter_$key"
    fun campsiteCard(campsiteId: String) = "search_campsite_card_$campsiteId"
}

/**
 * Displays the entrant campsite search screen backed by [EntrantSearchController], under the app header.
 *
 * @param controller Controller managing search query, filters, and matching campsites.
 * @param modifier Modifier applied to the root column.
 * @param roles Roles the user can switch to from the header. Empty hides the switcher.
 * @param onRoleSelected Called with the role picked in the header.
 */
@Composable
fun EntrantCampsiteSearchScreen(
    controller: EntrantSearchController,
    modifier: Modifier = Modifier,
    roles: List<UserRole> = emptyList(),
    onRoleSelected: (UserRole) -> Unit = {},
) {
    val imageRepo = remember { ImageRepository() }
    val currentFilter = controller.filter
    val displayedResults = controller.displayedResults
    val hasActiveFilters = currentFilter.locationName != null ||
        currentFilter.name != null ||
        (currentFilter.startDate != null && currentFilter.endDate != null) ||
        currentFilter.minOccupancy != null ||
        currentFilter.maxPricePerNight != null ||
        currentFilter.tags.isNotEmpty()

    Column(
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppHeader(UserRole.ENTRANT, roles, onRoleSelected)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppSearchBar(
                query = controller.searchQuery,
                onQueryChange = controller::onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag(EntrantSearchTestTags.SEARCH_BAR),
            )
            AppIconButton(
                icon = Icons.Filled.FilterList,
                contentDescription = "Filter for campsites based on location, capacity, price and more",
                onClick = controller::onOpenFilterClick,
                modifier = Modifier.testTag(EntrantSearchTestTags.FILTER_BUTTON),
            )
            AppIconButton(
                icon = Icons.Filled.QrCode2,
                contentDescription = "Find a campsite from promotional QR code",
                onClick = controller::onQrScanClick,
                modifier = Modifier.testTag(EntrantSearchTestTags.QR_SCAN_BUTTON),
            )
        }

        if (hasActiveFilters) {
            LazyRow(
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .testTag(EntrantSearchTestTags.ACTIVE_FILTERS_ROW),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentFilter.locationName?.let { name ->
                    item {
                        AppFilterTag(
                            text = name,
                            onClick = controller::removeLocationFilter,
                            modifier = Modifier.testTag(EntrantSearchTestTags.activeFilterTag("location")),
                        )
                    }
                }
                currentFilter.name?.let { name ->
                    item {
                        AppFilterTag(
                            text = name,
                            onClick = controller::removeNameFilter,
                            modifier = Modifier.testTag(EntrantSearchTestTags.activeFilterTag("name")),
                        )
                    }
                }
                if (currentFilter.startDate != null && currentFilter.endDate != null) {
                    item {
                        AppFilterTag(
                            text = "${currentFilter.startDate}-${currentFilter.endDate}",
                            onClick = controller::removeDateRangeFilter,
                            modifier = Modifier.testTag(EntrantSearchTestTags.activeFilterTag("dates")),
                        )
                    }
                }
                currentFilter.minOccupancy?.let { guests ->
                    item {
                        AppFilterTag(
                            text = "$guests guests",
                            onClick = controller::removeOccupancyFilter,
                            modifier = Modifier.testTag(EntrantSearchTestTags.activeFilterTag("occupancy")),
                        )
                    }
                }
                currentFilter.maxPricePerNight?.let { price ->
                    item {
                        AppFilterTag(
                            text = "\$$price",
                            onClick = controller::removeMaxPriceFilter,
                            modifier = Modifier.testTag(EntrantSearchTestTags.activeFilterTag("max_price")),
                        )
                    }
                }
                items(currentFilter.tags) { tag ->
                    AppFilterTag(
                        text = tag,
                        onClick = { controller.removeTagFilter(tag) },
                        modifier = Modifier.testTag(EntrantSearchTestTags.activeFilterTag("tag_$tag")),
                    )
                }
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
                modifier = Modifier.testTag(EntrantSearchTestTags.RESULTS_COUNT_TEXT),
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
                    modifier = Modifier.testTag(EntrantSearchTestTags.EMPTY_STATE_TEXT),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag(EntrantSearchTestTags.RESULTS_LIST),
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
                        },
                        modifier = Modifier.testTag(EntrantSearchTestTags.campsiteCard(campsite.id)),
                    )
                }
            }
        }
    }
}