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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

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
    private var loadedResults = emptyList<SearchResult>()
    private var movieSearchUnavailable = false

    fun initialize(initialType: SearchResultType?, selectedCity: String) {
        if (initialized && city == selectedCity) return
        city = selectedCity
        if (!initialized) {
            initialized = true
            _uiState.value = _uiState.value.copy(selectedTypes = initialType?.let(::setOf) ?: SearchResultType.entries.toSet())
        }
        loadedResults = emptyList()
        _uiState.value = _uiState.value.copy(results = emptyList(), total = 0)
        refresh()
    }

    fun onQueryChange(value: String) {
        if (_uiState.value.query == value) return
        _uiState.value = _uiState.value.copy(query = value)
        refresh(debounce = true)
    }
    fun clearQuery() = onQueryChange("")
    fun submitSearch() = refresh()
    fun toggleType(type: SearchResultType) {
        val types = _uiState.value.selectedTypes
        _uiState.value = _uiState.value.copy(selectedTypes = if (type in types) types - type else types + type)
        refresh()
    }
    fun setPriceFilter(filter: PriceFilter) { _uiState.value = _uiState.value.copy(priceFilter = filter); applyFilters() }
    fun setSort(sort: SearchSort) { _uiState.value = _uiState.value.copy(sort = sort); applyFilters() }
    fun resetFilters() {
        val needsReload = _uiState.value.selectedTypes.size != SearchResultType.entries.size
        _uiState.value = _uiState.value.copy(selectedTypes = SearchResultType.entries.toSet(), priceFilter = PriceFilter.ANY, sort = SearchSort.RELEVANCE)
        if (needsReload) refresh() else applyFilters()
    }
    fun retry() = refresh()

    private fun applyFilters() {
        val results = filterSearchResults(loadedResults, _uiState.value)
        _uiState.value = _uiState.value.copy(results = results, total = results.size)
    }

    private fun refresh(debounce: Boolean = false) {
        job?.cancel()
        if (_uiState.value.selectedTypes.isEmpty()) {
            loadedResults = emptyList()
            _uiState.value = _uiState.value.copy(isLoading = false, results = emptyList(), total = 0, errorMessage = null)
            return
        }
        val selectedCity = city
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        job = viewModelScope.launch {
        if (debounce) delay(300)
        val filters = buildMap {
            if (selectedCity.isNotBlank()) put("city", selectedCity)
            if (state.query.isNotBlank()) put("search", state.query.trim())
        }
        val batches = coroutineScope {
            listOf(SearchResultType.MOVIE, SearchResultType.EVENT, SearchResultType.SPORT).filter { it in state.selectedTypes }.map { type ->
                async {
                    when (type) {
                        SearchResultType.MOVIE -> loadMovies(state.query.trim(), filters)
                        SearchResultType.EVENT -> when (val result = backend.gateway.execute { listEvents(filters) }) {
                            is ApiResult.Success -> SearchBatch(result.value.data.orEmpty().mapNotNull { it.toUi() }.map { SearchResult(it, type) })
                            is ApiResult.Failure -> SearchBatch(error = result.userMessage)
                        }
                        SearchResultType.SPORT -> when (val result = backend.gateway.execute { listVenues(filters) }) {
                            is ApiResult.Success -> SearchBatch(result.value.data.orEmpty().mapNotNull { it.toUi() }.map { SearchResult(it, type) })
                            is ApiResult.Failure -> SearchBatch(error = result.userMessage)
                        }
                    }
                }
            }.awaitAll()
        }
        coroutineContext.ensureActive()
        loadedResults = batches.flatMap { it.results }.distinctBy { it.type to it.item.id }
        val errors = batches.mapNotNull { it.error }.distinct()
        val completedState = _uiState.value.copy(errorMessage = errors.takeIf { it.isNotEmpty() }?.joinToString("\n"), isLoading = false)
        val results = filterSearchResults(loadedResults, completedState)
        // Publish completion with its results so collectors never see an idle state
        // carrying the previous query's results (or the initial empty list).
        _uiState.value = completedState.copy(results = results, total = results.size)
        }
    }

    private suspend fun loadMovies(query: String, filters: Map<String, String>): SearchBatch {
        if (query.isNotBlank() && !movieSearchUnavailable) {
            when (val searched = backend.gateway.data { searchMovies(query) }) {
                is ApiResult.Success -> return SearchBatch(searched.value?.items.orEmpty().mapNotNull { it.toUi() }.map { SearchResult(it, SearchResultType.MOVIE) })
                is ApiResult.HttpError -> if (searched.httpCode == 404) movieSearchUnavailable = true else return SearchBatch(error = searched.userMessage)
                is ApiResult.Failure -> return SearchBatch(error = searched.userMessage)
            }
        }
        // The deployed search route can be shadowed by the detail route, while the
        // list endpoint can ignore search. Inspect bounded pages before filtering.
        val movies = linkedMapOf<String, SearchResult>()
        val incompleteMessage = "Some movie results could not be loaded. Please try again later."
        for (page in 1..10) {
            coroutineContext.ensureActive()
            val requestFilters = if (query.isBlank()) filters else filters + mapOf("page" to page.toString(), "pageSize" to "100")
            when (val listed = backend.gateway.execute { listMovies(requestFilters) }) {
                is ApiResult.Success -> {
                    val previousSize = movies.size
                    val items = listed.value.data.orEmpty()
                    items.mapNotNull { it.toUi() }.forEach { movies[it.id] = SearchResult(it, SearchResultType.MOVIE) }
                    val totalPages = listed.value.pagination?.totalPages ?: 1
                    if (query.isBlank()) return SearchBatch(movies.values.toList())
                    if (page > 1 && movies.size == previousSize) return SearchBatch(movies.values.toList(), incompleteMessage)
                    if (page >= totalPages) return SearchBatch(movies.values.toList())
                    if (items.isEmpty()) return SearchBatch(movies.values.toList(), incompleteMessage)
                }
                is ApiResult.Failure -> return SearchBatch(movies.values.toList(), listed.userMessage)
            }
        }
        return SearchBatch(movies.values.toList(), incompleteMessage)
    }
}

private data class SearchBatch(val results: List<SearchResult> = emptyList(), val error: String? = null)

internal fun filterSearchResults(results: List<SearchResult>, state: SearchUiState): List<SearchResult> {
    val query = state.query.trim()
    val filtered = results.filter { result ->
        result.type in state.selectedTypes && (query.isBlank() || listOf(result.item.title, result.item.location, result.item.date).any { it.contains(query, true) })
    }.map { it to searchPrice(it.item.price) }.filter { (_, amount) ->
        when (state.priceFilter) {
            PriceFilter.ANY -> true
            PriceFilter.FREE -> amount == 0.0
            PriceFilter.UNDER_500 -> amount != null && amount > 0.0 && amount < 500.0
            PriceFilter.ABOVE_500 -> amount != null && amount >= 500.0
        }
    }
    return when (state.sort) {
        SearchSort.RELEVANCE -> filtered
        SearchSort.PRICE_LOW -> filtered.sortedWith(compareBy<Pair<SearchResult, Double?>> { it.second == null }.thenBy { it.second })
        SearchSort.PRICE_HIGH -> filtered.sortedWith(compareBy<Pair<SearchResult, Double?>> { it.second == null }.thenByDescending { it.second })
    }.map { it.first }
}

private val priceAmount = Regex("\\d[\\d,]*(?:\\.\\d+)?")
internal fun searchPrice(price: String): Double? = if (price.contains("free", ignoreCase = true)) 0.0 else
    priceAmount.find(price)?.value?.replace(",", "")?.toDoubleOrNull()
