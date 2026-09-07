package com.entrymyslot.app.screens.movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslot.app.data.model.Movie
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

data class MovieDetailUiState(val isLoading: Boolean = false, val movie: Movie? = null, val isOffline: Boolean = false, val errorMessage: String? = null, val httpStatus: Int? = null)
class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val backend = (application as EntryMySlotApp).appContainer.backend
    private val state = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = state.asStateFlow()
    private var id = ""
    private var job: Job? = null
    fun loadMovie(id: String, force: Boolean = false) {
        if (!force && this.id == id && (job?.isActive == true || state.value.movie != null)) return
        this.id = id
        job?.cancel()
        job = viewModelScope.launch {
            state.value = MovieDetailUiState(isLoading = true)
            when (val result = backend.loadMovieForUi(id)) {
                is ApiResult.Success -> {
                    val item = result.value?.toUi()
                    state.value = MovieDetailUiState(movie = item, errorMessage = if (item == null) "Details are unavailable." else null)
                }
                is ApiResult.Failure -> state.value = MovieDetailUiState(isOffline = result is ApiResult.NoInternet, errorMessage = result.userMessage, httpStatus = (result as? ApiResult.HttpError)?.httpCode)
            }
        }
    }
    fun retry() = loadMovie(id, force = true)
}

// Compatibility for the deployed detail route's parameter mismatch; all values still come from the server.
internal suspend fun com.entrymyslotbe.app.EntryMySlotBackend.loadMovieForUi(id: String): ApiResult<com.entrymyslotbe.app.network.model.Movie?> {
    val detail = gateway.data { getMovie(id) }
    if (detail !is ApiResult.HttpError || detail.httpCode != 404) return detail
    val seenIds = mutableSetOf<String>()
    for (page in 1..10) {
        coroutineContext.ensureActive()
        when (val result = gateway.execute { listMovies(mapOf("page" to page.toString(), "pageSize" to "50")) }) {
            is ApiResult.Success -> {
                val movies = result.value.data.orEmpty()
                movies.firstOrNull { it.id == id || it.slug == id }?.let {
                    return ApiResult.Success(it, result.httpCode)
                }
                if (page >= (result.value.pagination?.totalPages ?: 1)) return detail
                val newIds = movies.mapNotNull { it.id }.filter { seenIds.add(it) }
                if (newIds.isEmpty()) {
                    return ApiResult.UnexpectedError("Unable to load movie details. Please try again later.")
                }
            }
            is ApiResult.Failure -> return result
        }
    }
    return ApiResult.UnexpectedError("Unable to load movie details. Please try again later.")
}
