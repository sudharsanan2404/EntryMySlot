package com.entrymyslot.app.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.booking.BookingApi
import com.entrymyslot.app.data.booking.BookingDto
import com.entrymyslot.app.data.model.Booking
import com.entrymyslot.app.data.model.BookingStatus
import com.entrymyslot.app.data.model.BookingType
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

data class BookingUiState(
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val selectedTab: Int = 0,
    val selectedFilter: String = "All",
    val bookings: List<Booking> = emptyList(),
    val total: Int = 0,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)

class BookingViewModel(
    private val bookingApi: BookingApi,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        BookingUiState(isOffline = !networkMonitor.isCurrentlyOnline())
    )
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private var requestJob: Job? = null

    init {
        viewModelScope.launch {
            var wasOnline = networkMonitor.isCurrentlyOnline()
            networkMonitor.isOnline.collect { isOnline ->
                val shouldRetry = isOnline && !wasOnline && _uiState.value.errorMessage != null
                wasOnline = isOnline
                _uiState.value = _uiState.value.copy(isOffline = !isOnline)
                if (shouldRetry) loadBookings()
            }
        }
    }

    fun loadBookings() {
        requestJob?.cancel()
        if (!networkMonitor.isCurrentlyOnline()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasLoaded = true,
                isOffline = true,
                errorMessage = "No internet connection. Reconnect and try again."
            )
            return
        }

        val snapshot = _uiState.value
        requestJob = viewModelScope.launch {
            _uiState.value = snapshot.copy(
                isLoading = true,
                isOffline = false,
                errorMessage = null
            )
            try {
                val response = bookingApi.getMyBookings(
                    tab = if (snapshot.selectedTab == 0) "upcoming" else "past",
                    type = snapshot.selectedFilter.toApiType()
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasLoaded = true,
                    bookings = response.data.mapNotNull(BookingDto::toBooking),
                    total = response.pagination.total,
                    errorMessage = null
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasLoaded = true,
                    isOffline = error is UnknownHostException,
                    errorMessage = error.toBookingMessage()
                )
            }
        }
    }

    fun selectTab(tab: Int) {
        if (tab == _uiState.value.selectedTab) return
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            bookings = emptyList(),
            total = 0,
            errorMessage = null
        )
        loadBookings()
    }

    fun selectFilter(filter: String) {
        if (filter == _uiState.value.selectedFilter) return
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            bookings = emptyList(),
            total = 0,
            errorMessage = null
        )
        loadBookings()
    }

    fun retry() {
        loadBookings()
    }

    private fun String.toApiType(): String = when (this) {
        "Movies" -> "movie"
        "Turf" -> "turf"
        "Events" -> "event"
        else -> "all"
    }

    private fun Throwable.toBookingMessage(): String = when (this) {
        is UnknownHostException -> "No internet connection. Reconnect and try again."
        is ConnectException -> "Booking server is unavailable right now. Please retry."
        is SocketTimeoutException -> "The server took too long to load your bookings."
        is HttpException -> when (code()) {
            401 -> "Your session has expired. Please sign in again."
            else -> "Unable to load bookings (server error ${code()})."
        }
        else -> message?.takeIf(String::isNotBlank)
            ?: "Unable to load your bookings. Please try again."
    }
}

private fun BookingDto.toBooking(): Booking? {
    val bookingType = when (type.uppercase()) {
        "MOVIE" -> BookingType.MOVIE
        "TURF" -> BookingType.TURF
        "EVENT" -> BookingType.EVENT
        else -> return null
    }
    val bookingStatus = when (status.uppercase()) {
        "UPCOMING" -> BookingStatus.UPCOMING
        "COMPLETED" -> BookingStatus.COMPLETED
        "CANCELLED" -> BookingStatus.CANCELLED
        else -> return null
    }
    return Booking(
        id = id,
        userId = userId,
        type = bookingType,
        itemId = itemId,
        venueId = venueId,
        dateTime = dateTime,
        details = details,
        price = price,
        status = bookingStatus,
        bookingReference = bookingReference,
        title = title,
        location = location
    )
}
