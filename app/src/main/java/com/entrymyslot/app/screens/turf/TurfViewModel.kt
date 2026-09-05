package com.entrymyslot.app.screens.turf
import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.Turf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
data class TurfDetailUiState(val isLoading: Boolean = false, val turf: Turf? = null, val isOffline: Boolean = false, val errorMessage: String? = null, val httpStatus: Int? = null)
class TurfViewModel : ViewModel() {
    private val state = MutableStateFlow(TurfDetailUiState()); val uiState: StateFlow<TurfDetailUiState> = state.asStateFlow(); private var id = ""
    fun loadTurf(id: String) { this.id = id; val item = FakeData.getTurfById(id); state.value = TurfDetailUiState(turf = item, errorMessage = if (item == null) "Venue preview is unavailable." else null) }
    fun retry() = loadTurf(id)
}
