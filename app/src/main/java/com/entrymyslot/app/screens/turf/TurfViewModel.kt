package com.entrymyslot.app.screens.turf

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslot.app.data.model.Turf
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TurfDetailUiState(val isLoading: Boolean = false, val turf: Turf? = null, val isOffline: Boolean = false, val errorMessage: String? = null, val httpStatus: Int? = null)
class TurfViewModel(application: Application) : AndroidViewModel(application) {
    private val backend = (application as EntryMySlotApp).appContainer.backend
    private val state = MutableStateFlow(TurfDetailUiState())
    val uiState: StateFlow<TurfDetailUiState> = state.asStateFlow()
    private var id = ""
    private var job: Job? = null
    fun loadTurf(id: String) {
        this.id = id
        job?.cancel()
        job = viewModelScope.launch {
            state.value = TurfDetailUiState(isLoading = true)
            val resource = when (val result = backend.gateway.execute { listVenues() }) {
                is ApiResult.Success -> result.value.data.orEmpty().firstOrNull { it.id == id }
                is ApiResult.Failure -> return@launch fail(result)
            } ?: return@launch fail(ApiResult.UnexpectedError("Resource is unavailable."))
            val venueId = resource.venueId ?: resource.id ?: return@launch
            val venue = when (val result = backend.gateway.data { getVenue(venueId) }) {
                is ApiResult.Success -> result.value
                is ApiResult.Failure -> return@launch fail(result)
            } ?: return@launch fail(ApiResult.UnexpectedError("Venue is unavailable."))
            val turf = resource.copy(description = venue.description, images = venue.images ?: resource.images,
                facilities = venue.facilities ?: resource.facilities, address = venue.address, city = venue.city).toUi()
            state.value = TurfDetailUiState(turf = turf)
            when (val reviews = backend.gateway.data { listVenueReviews(venueId) }) {
                is ApiResult.Success -> if (reviews.value != null) state.value = state.value.copy(turf = turf?.copy(reviewCount = reviews.value.size))
                is ApiResult.Failure -> state.value = state.value.copy(errorMessage = reviews.userMessage)
            }
        }
    }
    fun retry() = loadTurf(id)
    private fun fail(result: ApiResult.Failure) {
        state.value = TurfDetailUiState(isOffline = result is ApiResult.NoInternet, errorMessage = result.userMessage, httpStatus = (result as? ApiResult.HttpError)?.httpCode)
    }
}
