package com.example.lottcamp.ui.screens.entrant

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds and manages the active search query and [CampsiteFilter] state for the entrant search flow.
 */
class SearchViewModel : ViewModel() {
    private val _filter = MutableStateFlow(CampsiteFilter())
    val filter: StateFlow<CampsiteFilter> = _filter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Replaces the active [CampsiteFilter] criteria used to query matching campsites.
     *
     * @param filter The new filter configuration to apply.
     */
    fun setFilter(filter: CampsiteFilter) {
        _filter.value = filter
    }

    /**
     * Sanitizes and updates the free-text search query used to filter results by name or location.
     *
     * @param query Raw text entered by the user in the search bar.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = sanitizeSearchInput(query)
    }

    /**
     * Resets all active [CampsiteFilter] criteria back to their default values.
     */
    fun clearFilter() {
        _filter.value = CampsiteFilter()
    }

    companion object {
        private const val MAX_QUERY_LENGTH = 100

        /**
         * Strips control characters and Unicode non-character sentinel code points
         * (such as `\uFFFF`, `\uFFFE`, and `\uF8FF`) and caps length to prevent query manipulation.
         *
         * @param raw Untrusted input string from the search field.
         * @return A sanitized string safe for filtering and query bounds.
         */
        fun sanitizeSearchInput(raw: String): String {
            return raw
                .filterNot { ch ->
                    ch.isISOControl() || ch == '\uFFFF' || ch == '\uFFFE' || ch == '\uF8FF'
                }
                .take(MAX_QUERY_LENGTH)
        }
    }
}