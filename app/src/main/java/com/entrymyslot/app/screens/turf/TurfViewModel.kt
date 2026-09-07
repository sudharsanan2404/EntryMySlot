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
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import com.entrymyslotbe.app.network.model.Venue

data class TurfDetailUiState(val isLoading: Boolean = false, val turf: Turf? = null, val isOffline: Boolean = false, val errorMessage: String? = null, val httpStatus: Int? = null)
class TurfViewModel(application: Application) : AndroidViewModel(application) {
    private val backend = (application as EntryMySlotApp).appContainer.backend
    private val state = MutableStateFlow(TurfDetailUiState())
    val uiState: StateFlow<TurfDetailUiState> = state.asStateFlow()
    private var id = ""
    private var job: Job? = null
    fun loadTurf(id: String, force: Boolean = false) {
        if (!force && this.id == id && (job?.isActive == true || state.value.turf != null)) return
        this.id = id
        job?.cancel()
        job = viewModelScope.launch {
            state.value = TurfDetailUiState(isLoading = true)
            val resource = when (val result = findResource(id)) {
                is ApiResult.Success -> result.value
                is ApiResult.Failure -> return@launch fail(result)
            }
            val venueId = resource.venueId?.takeIf(String::isNotBlank) ?: resource.id
                ?: return@launch fail(ApiResult.UnexpectedError("Venue is unavailable."))
            val venue = when (val result = backend.gateway.data { getVenue(venueId) }) {
                is ApiResult.Success -> result.value
                is ApiResult.Failure -> return@launch fail(result)
            } ?: return@launch fail(ApiResult.UnexpectedError("Venue is unavailable."))
            val turf = resource.copy(description = venue.description ?: resource.description,
                images = venue.images?.takeIf { it.isNotEmpty() } ?: resource.images,
                facilities = venue.facilities ?: resource.facilities,
                address = venue.address ?: resource.address, city = venue.city ?: resource.city,
                rating = venue.rating ?: resource.rating, reviewCount = venue.reviewCount ?: resource.reviewCount).toUi()
            state.value = TurfDetailUiState(turf = turf, errorMessage = if (turf == null) "Turf details are unavailable." else null)
            when (val reviews = backend.gateway.data { listVenueReviews(venueId) }) {
                is ApiResult.Success -> if (reviews.value != null && venue.reviewCount == null && resource.reviewCount == null) state.value = state.value.copy(turf = turf?.copy(reviewCount = reviews.value.size))
                is ApiResult.Failure -> state.value = state.value.copy(errorMessage = reviews.userMessage)
            }
        }
    }
    fun retry() = loadTurf(id, force = true)

    private suspend fun findResource(id: String): ApiResult<Venue> {
        val seenIds = mutableSetOf<String>()
        for (page in 1..10) {
            coroutineContext.ensureActive()
            when (val result = backend.gateway.execute { listVenues(mapOf("page" to page.toString(), "pageSize" to "100")) }) {
                is ApiResult.Success -> {
                    val venues = result.value.data.orEmpty()
                    venues.firstOrNull { it.id == id }?.let { return ApiResult.Success(it, result.httpCode) }
                    if (page >= (result.value.pagination?.totalPages ?: 1)) break
                    val newIds = venues.mapNotNull { it.id }.filter { seenIds.add(it) }
                    if (newIds.isEmpty()) break
                }
                is ApiResult.Failure -> return result
            }
        }
        return ApiResult.UnexpectedError("Unable to load this venue. Please try again later.")
    }
    private fun fail(result: ApiResult.Failure) {
        state.value = TurfDetailUiState(isOffline = result is ApiResult.NoInternet, errorMessage = result.userMessage, httpStatus = (result as? ApiResult.HttpError)?.httpCode)
    }
}
