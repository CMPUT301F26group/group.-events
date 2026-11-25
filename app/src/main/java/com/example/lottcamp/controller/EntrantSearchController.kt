package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.LotteryRepository
import com.example.lottcamp.ui.screens.entrant.CampsiteFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

/**
 * Controller for entrant campsite discovery, free-text search, and filter management.
 *
 * Loads matching campsites from [campsiteRepo] whenever [filter] changes and applies
 * sanitized case-insensitive substring filtering in memory for [searchQuery].
 *
 * @param scope Coroutine scope used for asynchronous repository lookups.
 * @param campsiteRepo Repository used to query campsites.
 * @param lotteryRepo Repository used for lottery-backed filter checks.
 * @param navigate Navigation callback accepting a route string.
 * @param back Navigation callback to pop the current back stack entry.
 */
class EntrantSearchController(
    private val scope: CoroutineScope,
    private val campsiteRepo: CampsiteRepository = CampsiteRepository(),
    private val lotteryRepo: LotteryRepository = LotteryRepository(),
    private val navigate: (String) -> Unit = {},
    private val back: () -> Unit = {},
) {
    /** Active structured filter criteria. */
    var filter by mutableStateOf(CampsiteFilter())
        private set

    /** Sanitized free-text search bar query. */
    var searchQuery by mutableStateOf("")
        private set

    /** Campsites returned for the current [filter] before text search. */
    val baseResults = mutableStateListOf<Campsite>()

    /** Whether campsite results are currently loading. */
    var isLoading by mutableStateOf(false)
        private set

    /** Last error message, or null when clear. */
    var error by mutableStateOf<String?>(null)
        private set

    /** Campsites matching both [filter] and [searchQuery] (case-insensitive substring). */
    val displayedResults: List<Campsite>
        get() {
            val normalizedTerm = sanitizeSearchInput(searchQuery)
                .trim()
                .lowercase(Locale.getDefault())
            if (normalizedTerm.isEmpty()) return baseResults.toList()
            return baseResults.filter { campsite ->
                campsite.name.lowercase(Locale.getDefault()).contains(normalizedTerm) ||
                        campsite.locationName.lowercase(Locale.getDefault()).contains(normalizedTerm)
            }
        }

    /** Refreshes [baseResults] from Firestore using the active [filter]. */
    fun refreshResults() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val matches = filter.getMatches(campsiteRepo, lotteryRepo)
                baseResults.clear()
                baseResults.addAll(matches)
            } catch (e: Exception) {
                error = "Couldn't load campsites: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /** Updates [searchQuery] after sanitizing unsafe characters and length. */
    fun onSearchQueryChange(query: String) {
        searchQuery = sanitizeSearchInput(query)
    }

    /** Replaces [filter] and reloads matching campsites. */
    fun updateFilter(newFilter: CampsiteFilter) {
        filter = newFilter
        refreshResults()
    }

    /** Clears all active filter criteria and reloads campsites. */
    fun clearFilters() {
        filter = CampsiteFilter()
        refreshResults()
    }

    fun removeLocationFilter() = updateFilter(filter.copy(locationName = null))
    fun removeNameFilter() = updateFilter(filter.copy(name = null))
    fun removeDateRangeFilter() = updateFilter(filter.copy(startDate = null, endDate = null))
    fun removeOccupancyFilter() = updateFilter(filter.copy(minOccupancy = null))
    fun removeMaxPriceFilter() = updateFilter(filter.copy(maxPricePerNight = null))
    fun removeTagFilter(tag: String) = updateFilter(filter.copy(tags = filter.tags - tag))

    fun setLocationFilter(locationName: String?) = updateFilter(filter.copy(locationName = locationName))
    fun setNameFilter(name: String?) = updateFilter(filter.copy(name = name))
    fun setDateRangeFilter(startDate: LocalDate?, endDate: LocalDate?) =
        updateFilter(filter.copy(startDate = startDate, endDate = endDate))
    fun setMinOccupancyFilter(minOccupancy: Int?) = updateFilter(filter.copy(minOccupancy = minOccupancy))
    fun setMaxPriceFilter(maxPricePerNight: Double?) = updateFilter(filter.copy(maxPricePerNight = maxPricePerNight))
    fun toggleTagFilter(tag: String) {
        val nextTags = if (tag in filter.tags) filter.tags - tag else filter.tags + tag
        updateFilter(filter.copy(tags = nextTags))
    }

    fun onOpenFilterClick() = navigate(EntrantRoutes.FILTER)
    fun onQrScanClick() {}
    fun onCampsiteClick(campsiteId: String) = navigate(EntrantRoutes.campsiteDetails(campsiteId))
    fun onBackClick() = back()
    fun clearError() { error = null }

    companion object {
        private const val MAX_QUERY_LENGTH = 100

        /** Strips control characters and Unicode sentinel code points and caps input length. */
        fun sanitizeSearchInput(raw: String): String {
            return raw
                .filterNot { ch ->
                    ch.isISOControl() || ch == '\uFFFF' || ch == '\uFFFE' || ch == '\uF8FF'
                }
                .take(MAX_QUERY_LENGTH)
        }
    }
}