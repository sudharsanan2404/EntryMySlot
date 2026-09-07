package com.entrymyslot.app.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class SearchDiscoveryCard(val id: String, val label: String, val title: String, val subtitle: String, val type: SearchResultType)

private val searchDiscovery = listOf(
    SearchDiscoveryCard("discover-events", "LIVE", "Events around you", "Music, sports and experiences", SearchResultType.EVENT),
    SearchDiscoveryCard("discover-turf", "PLAY", "Book a turf", "Find your next available slot", SearchResultType.SPORT),
    SearchDiscoveryCard("discover-movies", "WATCH", "Now showing", "Discover films and showtimes", SearchResultType.MOVIE)
)

internal data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val selectedTypes: Set<SearchResultType> = SearchResultType.entries.toSet(),
    val priceFilter: PriceFilter = PriceFilter.ANY,
    val sort: SearchSort = SearchSort.RELEVANCE,
    val results: List<SearchResult> = emptyList(),
    val discoveryCards: List<SearchDiscoveryCard> = searchDiscovery,
    val total: Int = 0,
    val errorMessage: String? = null
)

internal class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val backend = (application as EntryMySlotApp).appContainer.backend
    private var city = ""
    private var job: Job? = null
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var initialized = false

    fun initialize(initialType: SearchResultType?, selectedCity: String) {
        city = selectedCity
        if (!initialized) {
            initialized = true
            _uiState.value = _uiState.value.copy(selectedTypes = initialType?.let(::setOf) ?: SearchResultType.entries.toSet())
        }
        refresh()
    }

    fun onQueryChange(value: String) { _uiState.value = _uiState.value.copy(query = value); refresh() }
    fun clearQuery() { _uiState.value = _uiState.value.copy(query = ""); refresh() }
    fun submitSearch() = refresh()
    fun toggleType(type: SearchResultType) {
        val types = _uiState.value.selectedTypes
        _uiState.value = _uiState.value.copy(selectedTypes = if (type in types) types - type else types + type)
        refresh()
    }
    fun setPriceFilter(filter: PriceFilter) { _uiState.value = _uiState.value.copy(priceFilter = filter); refresh() }
    fun setSort(sort: SearchSort) { _uiState.value = _uiState.value.copy(sort = sort); refresh() }
    fun resetFilters() {
        _uiState.value = _uiState.value.copy(selectedTypes = SearchResultType.entries.toSet(), priceFilter = PriceFilter.ANY, sort = SearchSort.RELEVANCE)
        refresh()
    }
    fun retry() = refresh()

    private fun refresh() {
        job?.cancel()
        job = viewModelScope.launch {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        // Debounce input; this is not simulated loading or success.
        delay(300)
        val errors = mutableListOf<String>()
        val filters = buildMap {
            if (city.isNotBlank()) put("city", city)
            if (state.query.isNotBlank()) put("search", state.query.trim())
        }
        val all = buildList {
            if (SearchResultType.MOVIE in state.selectedTypes) {
                val searched = if (state.query.isNotBlank()) backend.gateway.data { searchMovies(state.query.trim()) } else null
                val result = if (searched == null || (searched is ApiResult.HttpError && searched.httpCode == 404)) {
                    // The deployed /search route is shadowed by its detail route; listMovies supports the same server search filter.
                    when (val listed = backend.gateway.execute { listMovies(filters) }) {
                        is ApiResult.Success -> ApiResult.Success(listed.value.data.orEmpty(), listed.httpCode)
                        is ApiResult.Failure -> listed
                    }
                } else when (searched) {
                    is ApiResult.Success -> ApiResult.Success(searched.value?.items.orEmpty(), searched.httpCode)
                    is ApiResult.Failure -> searched
                }
                when (result) {
                    is ApiResult.Success -> addAll(result.value.mapNotNull { it.toUi() }.map { SearchResult(it, SearchResultType.MOVIE) })
                    is ApiResult.Failure -> errors.add(result.userMessage)
                }
            }
            if (SearchResultType.EVENT in state.selectedTypes) {
                when (val result = backend.gateway.execute { listEvents(filters) }) {
                    is ApiResult.Success -> addAll(result.value.data.orEmpty().mapNotNull { it.toUi() }.map { SearchResult(it, SearchResultType.EVENT) })
                    is ApiResult.Failure -> errors.add(result.userMessage)
                }
            }
            if (SearchResultType.SPORT in state.selectedTypes) {
                when (val result = backend.gateway.execute { listVenues(filters) }) {
                    is ApiResult.Success -> addAll(result.value.data.orEmpty().mapNotNull { it.toUi() }.map { SearchResult(it, SearchResultType.SPORT) })
                    is ApiResult.Failure -> errors.add(result.userMessage)
                }
            }
        }
        var results = all.filter { result ->
            result.type in state.selectedTypes && (state.query.isBlank() || listOf(result.item.title, result.item.location, result.item.date).any { it.contains(state.query.trim(), true) })
        }.filter { result ->
            val amount = result.item.price.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: -1.0
            when (state.priceFilter) {
                PriceFilter.ANY -> true
                PriceFilter.FREE -> result.item.price.contains("free", true) || amount == 0.0
                PriceFilter.UNDER_500 -> amount > 0.0 && amount < 500.0
                PriceFilter.ABOVE_500 -> amount >= 500
            }
        }
        results = when (state.sort) {
            SearchSort.RELEVANCE -> results
            SearchSort.PRICE_LOW -> results.sortedBy { it.item.price.filter { char -> char.isDigit() || char == '.' }.toDoubleOrNull() ?: Double.MAX_VALUE }
            SearchSort.PRICE_HIGH -> results.sortedByDescending { it.item.price.filter { char -> char.isDigit() || char == '.' }.toDoubleOrNull() ?: Double.MAX_VALUE }
        }
        _uiState.value = state.copy(results = results, total = results.size, errorMessage = errors.distinct().takeIf { it.isNotEmpty() }?.joinToString("\n"), isLoading = false)
        }
    }
}
