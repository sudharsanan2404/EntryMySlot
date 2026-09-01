package com.entrymyslot.app.screens.turf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.details.DetailsApi
import com.entrymyslot.app.data.details.toTurfModel
import com.entrymyslot.app.data.model.Turf
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

data class TurfDetailUiState(
    val isLoading: Boolean = true,
    val turf: Turf? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val httpStatus: Int? = null
)

class TurfViewModel(
    private val detailsApi: DetailsApi,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TurfDetailUiState(isOffline = !networkMonitor.isCurrentlyOnline())
    )
    val uiState: StateFlow<TurfDetailUiState> = _uiState.asStateFlow()

    private var requestJob: Job? = null
    private var resourceId: String? = null

    init {
        viewModelScope.launch {
            var wasOnline = networkMonitor.isCurrentlyOnline()
            networkMonitor.isOnline.collect { isOnline ->
                val shouldRetry = isOnline && !wasOnline && _uiState.value.errorMessage != null
                wasOnline = isOnline
                _uiState.value = _uiState.value.copy(isOffline = !isOnline)
                if (shouldRetry) resourceId?.let(::loadTurf)
            }
        }
    }

    fun loadTurf(id: String) {
        resourceId = id
        requestJob?.cancel()
        if (id.isBlank()) {
            _uiState.value = TurfDetailUiState(
                isLoading = false,
                errorMessage = "Invalid Turf resource ID. Return and select the venue again."
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
                val response = detailsApi.getTurfDetails(id)
                check(response.success) { "The server did not return Turf details." }
                _uiState.value = TurfDetailUiState(
                    isLoading = false,
                    turf = response.data.toTurfModel(),
                    httpStatus = 200
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOffline = error is UnknownHostException || !networkMonitor.isCurrentlyOnline(),
                    errorMessage = error.toTurfDetailMessage(),
                    httpStatus = (error as? HttpException)?.code()
                )
            }
        }
    }

    fun retry() {
        resourceId?.let(::loadTurf)
    }

    private fun Throwable.toTurfDetailMessage(): String = when (this) {
        is UnknownHostException -> "No internet connection. Reconnect and try again."
        is ConnectException -> "The Turf server is unavailable right now. Please retry."
        is SocketTimeoutException -> "The server took too long to load this venue."
        is HttpException -> when (code()) {
            404 -> "This Turf venue is no longer available."
            else -> "Unable to load Turf details (server error ${code()})."
        }
        else -> message?.takeIf(String::isNotBlank)
            ?: "Unable to load Turf details. Please try again."
    }
}
