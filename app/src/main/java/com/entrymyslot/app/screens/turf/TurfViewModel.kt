package com.entrymyslot.app.screens.turf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.model.SportsVenueDto
import com.entrymyslot.app.data.turf.TurfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TurfUiState(
    val venues: List<SportsVenueDto> = emptyList(),
    val selectedVenue: SportsVenueDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TurfViewModel(
    private val repository: TurfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TurfUiState())
    val uiState: StateFlow<TurfUiState> = _uiState.asStateFlow()

    init { loadVenues() }

    fun loadVenues() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        repository.listVenues(limit = 50)
            .onSuccess { (venues, _) ->
                _uiState.value = _uiState.value.copy(
                    venues = venues,
                    isLoading = false
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load venues"
                )
            }
    }

    fun loadVenueDetail(venueId: Long) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        repository.getVenueDetail(venueId)
            .onSuccess { venue ->
                _uiState.value = _uiState.value.copy(
                    selectedVenue = venue,
                    isLoading = false
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load venue"
                )
            }
    }
}
