package com.entrymyslot.app.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.event.EventRepository
import com.entrymyslot.app.data.model.EventDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventsUiState(
    val events: List<EventCardData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init { loadEvents() }

    fun loadEvents() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        repository.listEvents(limit = 50)
            .onSuccess { (events, _) ->
                _uiState.value = _uiState.value.copy(
                    events = events.map { it.toCardData() },
                    isLoading = false
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load events"
                )
            }
    }

    fun searchEvents(query: String) = viewModelScope.launch {
        if (query.isBlank()) {
            loadEvents()
            return@launch
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        repository.listEvents(search = query, limit = 50)
            .onSuccess { (events, _) ->
                _uiState.value = _uiState.value.copy(
                    events = events.map { it.toCardData() },
                    isLoading = false
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
    }
}

private fun EventDto.toCardData() = EventCardData(
    id = id,
    title = title,
    date = eventDate ?: startAt ?: "Date TBA",
    location = listOfNotNull(venue, city).filter { it.isNotBlank() }.joinToString(", "),
    price = if (isFree) "Free" else price ?: "TBD",
    imageUrl = thumbnailUrl ?: bannerUrl,
    isFree = isFree
)
