package com.entrymyslot.app.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.movie.MovieRepository
import com.entrymyslot.app.data.model.MovieDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoviesUiState(
    val movies: List<MovieCardData> = emptyList(),
    val genres: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MovieViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    init { loadMovies() }

    fun loadMovies() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        repository.listMovies(limit = 50)
            .onSuccess { (movies, _) ->
                _uiState.value = _uiState.value.copy(
                    movies = movies.map { it.toCardData() },
                    isLoading = false
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load movies"
                )
            }
    }

    fun getGenres() = viewModelScope.launch {
        repository.getGenres()
            .onSuccess { genres ->
                _uiState.value = _uiState.value.copy(genres = genres)
            }
    }
}

private fun MovieDto.toCardData() = MovieCardData(
    id = id,
    title = title,
    genre = genres.firstOrNull() ?: "",
    rating = averageRating?.toString(),
    posterUrl = posterUrl,
    language = language
)
