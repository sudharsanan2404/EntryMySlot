package com.entrymyslot.app.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.booking.*
import com.entrymyslot.app.data.model.Event
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslotbe.app.EntryMySlotBackend
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EventBookingOption(val id: String, val zoneId: String?, val name: String, val description: String, val pricePaise: Int, val remaining: Int, val currency: String, val tierId: String? = null, val minPerOrder: Int = 1, val maxPerOrder: Int = 10)
data class EventBookingUiState(
    val isLoading: Boolean = false, val isOffline: Boolean = false, val isHolding: Boolean = false,
    val event: Event? = null, val options: List<EventBookingOption> = emptyList(), val selectedOptionId: String? = null,
    val quantity: Int = 0, val attendees: List<PendingAttendee> = emptyList(), val validationMessage: String? = null,
    val errorMessage: String? = null, val httpStatus: Int? = null, val holdKey: String? = null, val holdExpiresAt: String? = null
) {
    val selectedOption get() = options.firstOrNull { it.id == selectedOptionId }
    val subtotalPaise get() = (selectedOption?.pricePaise ?: 0) * quantity
}
class EventBookingViewModel(private val backend: EntryMySlotBackend, private val pendingCheckoutStore: PendingCheckoutStore) : ViewModel() {
    private val state = MutableStateFlow(EventBookingUiState())
    val uiState: StateFlow<EventBookingUiState> = state.asStateFlow()
    private var eventId = ""
    private var job: Job? = null
    fun loadEvent(id: String) {
        eventId = id
        job?.cancel()
        job = viewModelScope.launch {
            state.value = EventBookingUiState(isLoading = true)
            val event = when (val result = backend.gateway.data { getEvent(id) }) {
                is ApiResult.Success -> result.value
                is ApiResult.Failure -> return@launch fail(result)
            } ?: return@launch fail(ApiResult.UnexpectedError("Event is unavailable."))
            val zones = when (val result = backend.gateway.execute { listZones(id) }) {
                is ApiResult.Success -> result.value.zones
                is ApiResult.Failure -> return@launch fail(result)
            }
            val options = if (zones.isNotEmpty()) zones.mapNotNull { zone ->
                val key = zone.id ?: return@mapNotNull null
                val price = zone.price?.movePointRight(2)?.intValueExact() ?: return@mapNotNull null
                EventBookingOption(key, key, zone.name.orEmpty(), zone.description.orEmpty(), price, zone.availableSeats ?: 0, zone.currency ?: event.currency ?: "INR")
            } else if (!event.ticketTiers.isNullOrEmpty()) event.ticketTiers.mapNotNull { tier ->
                val key = tier.id ?: return@mapNotNull null
                EventBookingOption(key, null, tier.name.orEmpty(), tier.description.orEmpty(), tier.price ?: return@mapNotNull null,
                    tier.availableQuantity ?: 0, tier.currency, key, tier.minPerOrder, tier.maxPerOrder)
            } else event.price?.let { price ->
                listOf(EventBookingOption(id, null, event.title.orEmpty(), event.description.orEmpty(), price.movePointRight(2).intValueExact(),
                    event.remainingCapacity ?: 0, event.currency ?: "INR"))
            }.orEmpty()
            state.value = EventBookingUiState(event = event.toUi(), options = options, selectedOptionId = options.firstOrNull { it.remaining > 0 }?.id)
        }
    }
    fun selectOption(optionId: String) { state.value = state.value.copy(selectedOptionId = optionId, quantity = 0, attendees = emptyList(), validationMessage = null) }
    fun setQuantity(quantity: Int) {
        val option = state.value.selectedOption ?: return; val count = quantity.coerceIn(0, minOf(option.remaining, option.maxPerOrder))
        state.value = state.value.copy(quantity = count, attendees = List(count) { state.value.attendees.getOrNull(it) ?: PendingAttendee("", "") }, validationMessage = null)
    }
    fun updateAttendee(index: Int, fullName: String? = null, phone: String? = null) {
        val list = state.value.attendees.toMutableList(); val current = list.getOrNull(index) ?: return
        list[index] = current.copy(fullName = fullName ?: current.fullName, phone = phone ?: current.phone)
        state.value = state.value.copy(attendees = list, validationMessage = null)
    }
    fun validateSelection(): Boolean {
        val error = when {
            state.value.quantity < (state.value.selectedOption?.minPerOrder ?: 1) -> "Select the minimum required ticket quantity."
            state.value.attendees.any { it.fullName.isBlank() || it.phone.length < 7 } -> "Enter a valid name and phone for every attendee."
            else -> null
        }
        state.value = state.value.copy(validationMessage = error); return error == null
    }

    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (!validateSelection()) return
        val current = state.value
        val event = current.event ?: return
        val option = current.selectedOption ?: return
        // The existing event API creates reservations at checkout; it has no hold endpoint.
        pendingCheckoutStore.save(PendingEventCheckout(event.id, event.title, "", "", option.zoneId, option.name,
            current.attendees, current.subtotalPaise, option.currency,
            AuthoritativeBillDto("EVENT", current.quantity, current.subtotalPaise, currency = option.currency), option.tierId))
        onSuccess()
    }
    fun releaseAndGoBack(onReleased: () -> Unit) {
        // A provider may have completed payment already. Back navigation must not cancel a booking.
        if (pendingCheckoutStore.payment == null) pendingCheckoutStore.clear()
        onReleased()
    }
    fun retry() = loadEvent(eventId)
    private fun fail(result: ApiResult.Failure) {
        state.value = state.value.copy(isLoading = false, isOffline = result is ApiResult.NoInternet, errorMessage = result.userMessage, httpStatus = (result as? ApiResult.HttpError)?.httpCode)
    }
}
