package com.entrymyslot.app.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.details.DetailsApi
import com.entrymyslot.app.data.details.toEventModel
import com.entrymyslot.app.data.model.Event
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

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val httpStatus: Int? = null
)

class EventViewModel(
    private val detailsApi: DetailsApi,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        EventDetailUiState(isOffline = !networkMonitor.isCurrentlyOnline())
    )
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private var requestJob: Job? = null
    private var eventId: String? = null

    init {
        viewModelScope.launch {
            var wasOnline = networkMonitor.isCurrentlyOnline()
            networkMonitor.isOnline.collect { isOnline ->
                val shouldRetry = isOnline && !wasOnline && _uiState.value.errorMessage != null
                wasOnline = isOnline
                _uiState.value = _uiState.value.copy(isOffline = !isOnline)
                if (shouldRetry) eventId?.let(::loadEvent)
            }
        }
    }

    fun loadEvent(id: String) {
        eventId = id
        requestJob?.cancel()
        if (id.isBlank()) {
            _uiState.value = EventDetailUiState(
                isLoading = false,
                errorMessage = "Invalid event ID. Return and select the event again."
            )
            return
        }
        if (!networkMonitor.isCurrentlyOnline()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOffline = true,
                errorMessage = "No internet connection. Reconnect and try again.",
                httpStatus = null
            )
            return
        }

        requestJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isOffline = false,
                errorMessage = null,
                httpStatus = null
            )
            try {
                val response = detailsApi.getEventDetails(id)
                check(response.success) { "The server did not return event details." }
                _uiState.value = EventDetailUiState(
                    isLoading = false,
                    event = response.data.toEventModel(),
                    httpStatus = 200
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOffline = error is UnknownHostException || !networkMonitor.isCurrentlyOnline(),
                    errorMessage = error.toEventDetailMessage(),
                    httpStatus = (error as? HttpException)?.code()
                )
            }
        }
    }

    fun retry() {
        eventId?.let(::loadEvent)
    }

    private fun Throwable.toEventDetailMessage(): String = when (this) {
        is UnknownHostException -> "No internet connection. Reconnect and try again."
        is ConnectException -> "The event server is unavailable right now. Please retry."
        is SocketTimeoutException -> "The server took too long to load this event."
        is HttpException -> when (code()) {
            404 -> "This event is no longer available."
            else -> "Unable to load event details (server error ${code()})."
        }
        else -> message?.takeIf(String::isNotBlank)
            ?: "Unable to load event details. Please try again."
    }
}
