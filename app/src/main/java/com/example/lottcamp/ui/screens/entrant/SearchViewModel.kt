package com.example.lottcamp.ui.screens.entrant

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel : ViewModel() {
    private val _filter = MutableStateFlow(CampsiteFilter())
    val filter: StateFlow<CampsiteFilter> = _filter.asStateFlow()

    fun setFilter(filter: CampsiteFilter) {
        _filter.value = filter
    }

    fun clearFilter() {
        _filter.value = CampsiteFilter()
    }
}