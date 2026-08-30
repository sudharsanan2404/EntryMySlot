package com.entrymyslot.app.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.booking.BookingRepository
import com.entrymyslot.app.data.model.EventBookingListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingUiState(
    val eventBookings: List<BookingItem> = emptyList(),
    val movieBookings: List<BookingItem> = emptyList(),
    val turfBookings: List<BookingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class BookingViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init { loadAllBookings() }

    fun loadAllBookings() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        repository.getMyBookings()
            .onSuccess { items ->
                _uiState.value = _uiState.value.copy(
                    eventBookings = items.map { it.toBookingItem() },
                    movieBookings = emptyList(),
                    turfBookings = emptyList(),
                    isLoading = false
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load bookings"
                )
            }
    }
}

private fun EventBookingListItem.toBookingItem() = BookingItem(
    id = bookingId,
    type = "Event",
    title = eventTitle,
    date = createdAt,
    status = status,
    amount = totalAmount
)
