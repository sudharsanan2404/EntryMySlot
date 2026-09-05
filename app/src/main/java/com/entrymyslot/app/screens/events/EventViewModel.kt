package com.entrymyslot.app.screens.events
import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
data class EventDetailUiState(val isLoading: Boolean = false, val event: Event? = null, val isOffline: Boolean = false, val errorMessage: String? = null, val httpStatus: Int? = null)
class EventViewModel : ViewModel() {
    private val state = MutableStateFlow(EventDetailUiState()); val uiState: StateFlow<EventDetailUiState> = state.asStateFlow(); private var id = ""
    fun loadEvent(id: String) { this.id = id; val item = FakeData.getEventById(id); state.value = EventDetailUiState(event = item, errorMessage = if (item == null) "Event preview is unavailable." else null) }
    fun retry() = loadEvent(id)
}
