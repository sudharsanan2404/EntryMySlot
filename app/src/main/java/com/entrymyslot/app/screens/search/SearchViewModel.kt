package com.entrymyslot.app.screens.search

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class SearchDiscoveryCard(val id: String, val label: String, val title: String, val subtitle: String, val type: SearchResultType)

private val previewDiscovery = listOf(
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
    val discoveryCards: List<SearchDiscoveryCard> = previewDiscovery,
    val total: Int = 0,
    val errorMessage: String? = null
)

internal class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var initialized = false

    fun initialize(initialType: SearchResultType?, @Suppress("UNUSED_PARAMETER") selectedCity: String) {
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
        val state = _uiState.value
        val all = buildList {
            addAll(FakeData.movies.map { SearchResult(it, SearchResultType.MOVIE) })
            addAll(FakeData.turfs.map { SearchResult(it, SearchResultType.SPORT) })
            addAll(FakeData.events.map { SearchResult(it, SearchResultType.EVENT) })
        }
        var results = all.filter { result ->
            result.type in state.selectedTypes && (state.query.isBlank() || listOf(result.item.title, result.item.location, result.item.date).any { it.contains(state.query.trim(), true) })
        }.filter { result ->
            val amount = result.item.price.filter(Char::isDigit).toIntOrNull() ?: 0
            when (state.priceFilter) {
                PriceFilter.ANY -> true
                PriceFilter.FREE -> result.item.price.contains("free", true) || amount == 0
                PriceFilter.UNDER_500 -> amount in 1..499
                PriceFilter.ABOVE_500 -> amount >= 500
            }
        }
        results = when (state.sort) {
            SearchSort.RELEVANCE -> results
            SearchSort.PRICE_LOW -> results.sortedBy { it.item.price.filter(Char::isDigit).toIntOrNull() ?: 0 }
            SearchSort.PRICE_HIGH -> results.sortedByDescending { it.item.price.filter(Char::isDigit).toIntOrNull() ?: 0 }
        }
        _uiState.value = state.copy(results = results, total = results.size, errorMessage = null, isLoading = false)
    }
}
