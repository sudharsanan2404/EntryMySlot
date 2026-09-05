package com.entrymyslot.app.screens.movies
import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
data class MovieDetailUiState(val isLoading: Boolean = false, val movie: Movie? = null, val isOffline: Boolean = false, val errorMessage: String? = null, val httpStatus: Int? = null)
class MovieViewModel : ViewModel() {
    private val state = MutableStateFlow(MovieDetailUiState()); val uiState: StateFlow<MovieDetailUiState> = state.asStateFlow(); private var id = ""
    fun loadMovie(id: String) { this.id = id; val item = FakeData.getMovieById(id); state.value = MovieDetailUiState(movie = item, errorMessage = if (item == null) "Movie preview is unavailable." else null) }
    fun retry() = loadMovie(id)
}
