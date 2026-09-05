package com.entrymyslot.app.screens.booking

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.Booking
import com.entrymyslot.app.data.model.BookingStatus
import com.entrymyslot.app.data.model.BookingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BookingUiState(
    val isLoading: Boolean = false, val hasLoaded: Boolean = true, val selectedTab: Int = 0,
    val selectedFilter: String = "All", val bookings: List<Booking> = emptyList(), val total: Int = 0,
    val isOffline: Boolean = false, val errorMessage: String? = null
)

class BookingViewModel : ViewModel() {
    private val state = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = state.asStateFlow()
    init { loadBookings() }
    fun loadBookings() {
        val current = state.value
        val result = FakeData.bookings.filter { booking ->
            val tabMatches = if (current.selectedTab == 0) booking.status == BookingStatus.UPCOMING else booking.status != BookingStatus.UPCOMING
            val typeMatches = when (current.selectedFilter) {
                "Movies" -> booking.type == BookingType.MOVIE
                "Turf" -> booking.type == BookingType.TURF
                "Events" -> booking.type == BookingType.EVENT
                else -> true
            }
            tabMatches && typeMatches
        }
        state.value = current.copy(bookings = result, total = result.size, isLoading = false, hasLoaded = true, errorMessage = null)
    }
    fun selectTab(tab: Int) { state.value = state.value.copy(selectedTab = tab); loadBookings() }
    fun selectFilter(filter: String) { state.value = state.value.copy(selectedFilter = filter); loadBookings() }
    fun retry() = loadBookings()
}
