package com.entrymyslot.app.screens.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.mapper.toUiBooking
import com.entrymyslot.app.data.model.Booking
import com.entrymyslot.app.data.model.BookingStatus
import com.entrymyslot.app.data.model.BookingType
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BookingUiState(
    val isLoading: Boolean = false, val hasLoaded: Boolean = false, val selectedTab: Int = 0,
    val selectedFilter: String = "All", val bookings: List<Booking> = emptyList(), val total: Int = 0,
    val isOffline: Boolean = false, val errorMessage: String? = null,
    val allBookings: List<Booking> = emptyList(), val countsComplete: Boolean = false
)
class BookingViewModel(application: Application) : AndroidViewModel(application) {
    private val backend = (application as EntryMySlotApp).appContainer.backend
    private val state = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = state.asStateFlow()
    private var job: Job? = null
    init { loadBookings() }
    fun loadBookings() {
        job?.cancel()
        job = viewModelScope.launch {
            state.value = state.value.copy(isLoading = true, errorMessage = null)
            val all = mutableListOf<Booking>()
            val errors = mutableListOf<ApiResult.Failure>()
            var page = 1
            do {
                val result = backend.gateway.execute { getMyBookings(mapOf("page" to page.toString(), "pageSize" to "50")) }
                if (result is ApiResult.Success) {
                    all.addAll(result.value.data.orEmpty().mapNotNull { it.toUiBooking() })
                    if (page >= (result.value.pagination?.totalPages ?: 1)) break
                    page++
                } else { errors.add(result as ApiResult.Failure); break }
            } while (true)
            when (val result = backend.gateway.data { getMyEventBookings() }) {
                is ApiResult.Success -> all.addAll(result.value.orEmpty().mapNotNull { it.toUiBooking() })
                is ApiResult.Failure -> errors.add(result)
            }
            page = 1
            do {
                val result = backend.gateway.data { getMyTurfBookings(mapOf("page" to page.toString(), "pageSize" to "50")) }
                if (result is ApiResult.Success) {
                    all.addAll(result.value?.items.orEmpty().mapNotNull { it.toUiBooking() })
                    if (page >= (result.value?.totalPages ?: 1)) break
                    page++
                } else { errors.add(result as ApiResult.Failure); break }
            } while (true)
            state.value = state.value.copy(isLoading = false, hasLoaded = true, allBookings = all,
                countsComplete = errors.isEmpty(), isOffline = errors.any { it is ApiResult.NoInternet },
                errorMessage = errors.map { it.userMessage }.distinct().takeIf { it.isNotEmpty() }?.joinToString("\n"))
            filter()
        }
    }
    private fun filter() {
        val current = state.value
        val result = current.allBookings.filter { booking ->
            val upcoming = booking.status in setOf(BookingStatus.UPCOMING, BookingStatus.PENDING)
            val tabMatches = if (current.selectedTab == 0) upcoming else !upcoming
            val typeMatches = when (current.selectedFilter) {
                "Movies" -> booking.type == BookingType.MOVIE
                "Turf" -> booking.type == BookingType.TURF
                "Events" -> booking.type == BookingType.EVENT
                else -> true
            }
            tabMatches && typeMatches
        }
        state.value = current.copy(bookings = result, total = current.allBookings.size)
    }
    fun cancel(booking: Booking) {
        viewModelScope.launch {
            val result = when (booking.type) {
                BookingType.MOVIE -> backend.gateway.data { cancelBooking(booking.bookingReference) }
                BookingType.EVENT -> backend.gateway.data { cancelEventBooking(booking.id) }
                BookingType.TURF -> backend.gateway.data { cancelTurfBooking(booking.id) }
            }
            when (result) {
                is ApiResult.Success -> loadBookings()
                is ApiResult.Failure -> state.value = state.value.copy(errorMessage = result.userMessage)
            }
        }
    }
    fun selectTab(tab: Int) { state.value = state.value.copy(selectedTab = tab); filter() }
    fun selectFilter(filter: String) { state.value = state.value.copy(selectedFilter = filter); filter() }
    fun retry() = loadBookings()
}
