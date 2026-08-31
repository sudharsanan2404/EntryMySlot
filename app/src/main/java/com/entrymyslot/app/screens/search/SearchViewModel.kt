package com.entrymyslot.app.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.model.CatalogItem
import com.entrymyslot.app.data.search.SearchApi
import com.entrymyslot.app.data.search.SearchDiscoveryCardDto
import com.entrymyslot.app.data.search.SearchItemDto
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

internal data class SearchDiscoveryCard(
    val id: String,
    val label: String,
    val title: String,
    val subtitle: String,
    val type: SearchResultType
)

internal data class SearchUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedTypes: Set<SearchResultType> = SearchResultType.entries.toSet(),
    val priceFilter: PriceFilter = PriceFilter.ANY,
    val sort: SearchSort = SearchSort.RELEVANCE,
    val results: List<SearchResult> = emptyList(),
    val discoveryCards: List<SearchDiscoveryCard> = emptyList(),
    val total: Int = 0,
    val errorMessage: String? = null
)

internal class SearchViewModel(
    private val searchApi: SearchApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val seed = UUID.randomUUID().toString()
    private var city = ""
    private var initializedType: SearchResultType? = null
    private var initialized = false
    private var debounceJob: Job? = null
    private var requestJob: Job? = null

    fun initialize(initialType: SearchResultType?, selectedCity: String) {
        val normalizedCity = selectedCity.trim()
        if (initialized && initializedType == initialType && city == normalizedCity) return
        initialized = true
        initializedType = initialType
        city = normalizedCity
        _uiState.value = _uiState.value.copy(
            selectedTypes = initialType?.let(::setOf) ?: SearchResultType.entries.toSet()
        )
        load()
    }

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
        debounceJob?.cancel()
        requestJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            debounceJob = null
            load()
        }
    }

    fun clearQuery() {
        _uiState.value = _uiState.value.copy(query = "")
        load()
    }

    fun submitSearch() {
        load()
    }

    fun toggleType(type: SearchResultType) {
        val current = _uiState.value.selectedTypes
        _uiState.value = _uiState.value.copy(
            selectedTypes = if (type in current) current - type else current + type
        )
        load()
    }

    fun setPriceFilter(filter: PriceFilter) {
        _uiState.value = _uiState.value.copy(priceFilter = filter)
        load()
    }

    fun setSort(sort: SearchSort) {
        _uiState.value = _uiState.value.copy(sort = sort)
        load()
    }

    fun resetFilters() {
        _uiState.value = _uiState.value.copy(
            selectedTypes = SearchResultType.entries.toSet(),
            priceFilter = PriceFilter.ANY,
            sort = SearchSort.RELEVANCE
        )
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        debounceJob?.cancel()
        requestJob?.cancel()
        val snapshot = _uiState.value
        requestJob = viewModelScope.launch {
            _uiState.value = snapshot.copy(isLoading = true, errorMessage = null)
            try {
                val response = searchApi.search(
                    query = snapshot.query.trim(),
                    types = snapshot.selectedTypes.joinToString(",") { it.apiValue },
                    price = snapshot.priceFilter.apiValue,
                    sort = snapshot.sort.apiValue,
                    city = city,
                    seed = seed
                ).data
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = response.items.mapNotNull(SearchItemDto::toSearchResult),
                    discoveryCards = response.discoveryCards.mapNotNull(SearchDiscoveryCardDto::toUiModel),
                    total = response.total,
                    errorMessage = null
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.toSearchMessage()
                )
            }
        }
    }

    private fun Throwable.toSearchMessage(): String = when (this) {
        is UnknownHostException -> "No internet connection. Check your network and try again."
        is ConnectException -> "Server is unavailable right now. Please try again."
        is SocketTimeoutException -> "The server took too long to respond. Please retry."
        is HttpException -> "Search failed (server error ${code()}). Please try again."
        else -> message?.takeIf { it.isNotBlank() }
            ?: "Unable to search right now. Please try again."
    }
}

private data class SearchCatalogItem(
    override val id: String,
    override val title: String,
    override val date: String,
    override val location: String,
    override val price: String,
    override val imageUrl: String?
) : CatalogItem

private fun SearchItemDto.toSearchResult(): SearchResult? {
    val resultType = type.toSearchResultType() ?: return null
    return SearchResult(
        item = SearchCatalogItem(id, title, date, location, price, imageUrl),
        type = resultType
    )
}

private fun SearchDiscoveryCardDto.toUiModel(): SearchDiscoveryCard? {
    val resultType = type.toSearchResultType() ?: return null
    return SearchDiscoveryCard(
        id = id,
        label = label,
        title = title,
        subtitle = subtitle,
        type = resultType
    )
}

private fun String.toSearchResultType(): SearchResultType? = when (lowercase()) {
    "movie" -> SearchResultType.MOVIE
    "sport" -> SearchResultType.SPORT
    "event" -> SearchResultType.EVENT
    else -> null
}
