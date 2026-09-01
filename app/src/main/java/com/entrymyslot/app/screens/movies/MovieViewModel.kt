package com.entrymyslot.app.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.details.DetailsApi
import com.entrymyslot.app.data.details.toMovieModel
import com.entrymyslot.app.data.model.Movie
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val movie: Movie? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val httpStatus: Int? = null
)

class MovieViewModel(
    private val detailsApi: DetailsApi,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MovieDetailUiState(isOffline = !networkMonitor.isCurrentlyOnline())
    )
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private var requestJob: Job? = null
    private var movieId: String? = null

    init {
        viewModelScope.launch {
            var wasOnline = networkMonitor.isCurrentlyOnline()
            networkMonitor.isOnline.collect { isOnline ->
                val shouldRetry = isOnline && !wasOnline && _uiState.value.errorMessage != null
                wasOnline = isOnline
                _uiState.value = _uiState.value.copy(isOffline = !isOnline)
                if (shouldRetry) movieId?.let(::loadMovie)
            }
        }
    }

    fun loadMovie(id: String) {
        movieId = id
        requestJob?.cancel()
        if (id.isBlank()) {
            _uiState.value = MovieDetailUiState(
                isLoading = false,
                errorMessage = "Invalid movie ID. Return and select the movie again."
            )
            return
        }
        if (!networkMonitor.isCurrentlyOnline()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOffline = true,
                errorMessage = "No internet connection. Reconnect and try again.",
                httpStatus = null
            )
            return
        }

        requestJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isOffline = false,
                errorMessage = null,
                httpStatus = null
            )
            try {
                val response = detailsApi.getMovieDetails(id)
                check(response.success) { "The server did not return movie details." }
                _uiState.value = MovieDetailUiState(
                    isLoading = false,
                    movie = response.data.toMovieModel(),
                    httpStatus = 200
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOffline = error is UnknownHostException || !networkMonitor.isCurrentlyOnline(),
                    errorMessage = error.toMovieDetailMessage(),
                    httpStatus = (error as? HttpException)?.code()
                )
            }
        }
    }

    fun retry() {
        movieId?.let(::loadMovie)
    }

    private fun Throwable.toMovieDetailMessage(): String = when (this) {
        is UnknownHostException -> "No internet connection. Reconnect and try again."
        is ConnectException -> "The movie server is unavailable right now. Please retry."
        is SocketTimeoutException -> "The server took too long to load this movie."
        is HttpException -> when (code()) {
            404 -> "This movie is no longer available."
            else -> "Unable to load movie details (server error ${code()})."
        }
        else -> message?.takeIf(String::isNotBlank)
            ?: "Unable to load movie details. Please try again."
    }
}
