package com.entrymyslot.app.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.RetrofitClient
import com.entrymyslot.app.data.model.ApiResponse
import com.entrymyslot.app.data.model.EventDto
import com.entrymyslot.app.data.home.HomeApi
import com.entrymyslot.app.data.model.MovieDto
import com.entrymyslot.app.data.model.SportsVenueDto
import com.entrymyslot.app.data.model.HomeContent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
                val eventsDeferred = async { api.featuredEvents().contentOrThrow() }
                val moviesDeferred = async { api.featuredMovies().contentOrThrow() }
                val sportsDeferred = async { api.nearbySports().contentOrThrow() }
                
                val eventsList = eventsDeferred.await().map { it.toHomeContent() }
                val moviesList = moviesDeferred.await().map { it.toHomeContent() }
                val sportsList = sportsDeferred.await().map { it.toHomeContent() }
                
                Triple(eventsList, moviesList, sportsList)
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
        refresh()
    }
}

private fun EventDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = title,
    date = eventDate ?: startAt ?: "Date TBA",
    location = listOfNotNull(venue, city).filter { it.isNotBlank() }.joinToString(", "),
    price = if (isFree) "Free" else price?.let { "From ₹$it" } ?: "Book Now",
    imageUrl = thumbnailUrl ?: bannerUrl
)

private fun MovieDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = title,
    date = releaseDate ?: "Now Showing",
    location = language ?: "In cinemas",
    price = "Book Now",
    imageUrl = posterUrl ?: bannerUrl
)

private fun SportsVenueDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = name,
    date = venueType,
    location = listOfNotNull(address, city).filter { it.isNotBlank() }.joinToString(", "),
    price = "Book Slot",
    imageUrl = thumbnailUrl ?: bannerUrl
)

private fun <T> Response<ApiResponse<T>>.contentOrThrow(): T {
    if (!isSuccessful) throw IOException("The server returned HTTP ${code()}")
    val payload = body() ?: throw IOException("The server returned an empty response")
    if (!payload.success) throw IOException(payload.message ?: "Unable to load home content")
    return payload.data ?: throw IOException("The server returned no data")
}
