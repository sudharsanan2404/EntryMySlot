package com.entrymyslot.app.screens.turf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.booking.*
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslot.app.data.model.Turf
import com.entrymyslotbe.app.EntryMySlotBackend
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

data class TurfBookingUiState(
    val isLoading: Boolean = false, val isHolding: Boolean = false, val isOffline: Boolean = false,
    val turf: Turf? = null, val selectedDate: LocalDate = LocalDate.now(), val slots: List<TurfSlotDto> = emptyList(),
    val selectedUnitId: Int? = null, val holdToken: String? = null, val holdExpiresAt: String? = null,
    val holdSecondsRemaining: Int = 0, val errorMessage: String? = null, val httpStatus: Int? = null
) { val selectedSlot get() = slots.firstOrNull { it.unit_id == selectedUnitId } }

class TurfBookingViewModel(private val backend: EntryMySlotBackend, private val pendingCheckoutStore: PendingCheckoutStore) : ViewModel() {
    private val state = MutableStateFlow(TurfBookingUiState())
    val uiState: StateFlow<TurfBookingUiState> = state.asStateFlow()
    private var resourceId = ""
    private var job: Job? = null
    fun loadTurf(id: String, date: LocalDate = state.value.selectedDate) {
        resourceId = id
        job?.cancel()
        job = viewModelScope.launch {
            state.value = TurfBookingUiState(isLoading = true, selectedDate = date)
            val turf = when (val result = backend.gateway.execute { listVenues() }) {
                is ApiResult.Success -> result.value.data.orEmpty().firstOrNull { it.id == id }?.toUi()
                is ApiResult.Failure -> return@launch fail(result)
            } ?: return@launch fail(ApiResult.UnexpectedError("Resource is unavailable."))
            state.value = state.value.copy(turf = turf)
            val slots = when (val result = backend.gateway.data { getResourceAvailability(id, date.toString()) }) {
                is ApiResult.Success -> result.value?.slots.orEmpty().mapNotNull { slot ->
                    TurfSlotDto(slot.unit_id?.toIntOrNull() ?: return@mapNotNull null,
                        slot.starts_at ?: return@mapNotNull null, slot.ends_at ?: return@mapNotNull null,
                        slot.status ?: return@mapNotNull null, slot.price, slot.currency ?: "INR",
                        slot.formatted_time.orEmpty(), slot.duration_minutes ?: 0, slot.blocked_reason)
                }
                is ApiResult.Failure -> return@launch fail(result)
            }
            state.value = state.value.copy(isLoading = false, slots = slots)
        }
    }
    fun changeDate(date: LocalDate) = loadTurf(resourceId, date)
    fun onSlotClicked(slot: TurfSlotDto) {
        if (slot.status != "available" || state.value.isLoading) return
        state.value = state.value.copy(selectedUnitId = if (state.value.selectedUnitId == slot.unit_id) null else slot.unit_id, errorMessage = null)
    }
    fun validateSelection(): Boolean {
        val valid = state.value.selectedSlot?.let { it.status == "available" && it.price != null } == true
        if (!valid) state.value = state.value.copy(errorMessage = "Select an available slot with a server price before continuing.")
        return valid
    }
    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (!validateSelection()) return
        val current = state.value
        val turf = current.turf ?: return
        val slot = current.selectedSlot ?: return
        val subtotal = java.math.BigDecimal.valueOf(slot.price!!).movePointRight(2).intValueExact()
        // This API reserves the availability unit when createTurfBooking runs at checkout.
        pendingCheckoutStore.save(PendingTurfCheckout(turf.id, turf.title, slot.unit_id, slot.starts_at, slot.ends_at,
            slot.formatted_time, "", "", subtotal, slot.currency, AuthoritativeBillDto("TURF", 1, subtotal, currency = slot.currency)))
        onSuccess()
    }
    fun releaseAndGoBack(onReleased: () -> Unit) {
        // A provider may have completed payment already. Back navigation must not cancel a booking.
        if (pendingCheckoutStore.payment == null) pendingCheckoutStore.clear()
        onReleased()
    }
    fun retry() = loadTurf(resourceId, state.value.selectedDate)
    private fun fail(result: ApiResult.Failure) {
        state.value = state.value.copy(isLoading = false, isOffline = result is ApiResult.NoInternet, errorMessage = result.userMessage, httpStatus = (result as? ApiResult.HttpError)?.httpCode)
    }
}
