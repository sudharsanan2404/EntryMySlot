package com.entrymyslot.app.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.RetrofitClient
import com.entrymyslot.app.data.home.ApiResponse
import com.entrymyslot.app.data.home.EventDto
import com.entrymyslot.app.data.home.HomeApi
import com.entrymyslot.app.data.home.MovieDto
import com.entrymyslot.app.data.home.SportsVenueDto
import com.entrymyslot.app.data.model.HomeContent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.Response
import java.io.IOException

data class HomeUiState(
    val events: List<HomeContent> = emptyList(),
    val movies: List<HomeContent> = emptyList(),
    val sports: List<HomeContent> = emptyList(),
    val selectedCity: String = "Chennai",
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(private val api: HomeApi = RetrofitClient.homeApi) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching {
            coroutineScope {
                val events = async { api.featuredEvents().contentOrThrow().map { it.toHomeContent() } }
                val movies = async { api.featuredMovies().contentOrThrow().map { it.toHomeContent() } }
                val sports = async { api.nearbySports().contentOrThrow().map { it.toHomeContent() } }
                Triple(events.await(), movies.await(), sports.await())
            }
        }.onSuccess { (events, movies, sports) ->
            _uiState.value = _uiState.value.copy(
                events = events,
                movies = movies,
                sports = sports,
                isLoading = false
            )
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(isLoading = false, error = error.message ?: "Could not load home content")
        }
    }

    fun updateCity(city: String) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        // Optionally refresh content based on city
        refresh()
    }
}

private fun EventDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = title,
    date = eventDate ?: startAt ?: "Date to be announced",
    location = listOfNotNull(venue, city).filter(String::isNotBlank).joinToString(", "),
    price = if (isFree) "Free" else price.asPrice(),
    imageUrl = thumbnailUrl ?: bannerUrl
)

private fun MovieDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = title,
    date = status?.replace('_', ' ') ?: "Coming soon",
    location = language ?: "In cinemas",
    price = "Book tickets",
    imageUrl = posterUrl ?: backdropUrl
)

private fun SportsVenueDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = venueName?.takeIf(String::isNotBlank) ?: name ?: "Sports venue",
    date = category ?: "Available today",
    location = listOfNotNull(address, city).filter(String::isNotBlank).joinToString(", "),
    price = basePrice.asPrice() + " / hour"
)

private fun JsonElement?.asPrice(): String {
    val amount = (this as? JsonPrimitive)?.content?.toDoubleOrNull() ?: return "Free"
    return if (amount <= 0) "Free" else "From ₹" + if (amount % 1.0 == 0.0) amount.toInt() else amount
}

private fun <T> Response<ApiResponse<T>>.contentOrThrow(): T {
    if (!isSuccessful) throw IOException("The server returned HTTP ${code()}")
    val payload = body() ?: throw IOException("The server returned an empty response")
    if (!payload.success) throw IOException(payload.message ?: "Unable to load home content")
    return payload.data ?: throw IOException("The server returned no data")
}
