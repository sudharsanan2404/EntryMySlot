package com.entrymyslot.app.screens.events

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.booking.PendingAttendee
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingEventCheckout
import com.entrymyslot.app.data.model.Event
import com.entrymyslot.app.screens.movies.previewBill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

data class EventBookingOption(val id: String, val zoneId: Int?, val name: String, val description: String, val pricePaise: Int, val remaining: Int, val currency: String)
data class EventBookingUiState(
    val isLoading: Boolean = false, val isOffline: Boolean = false, val isHolding: Boolean = false,
    val event: Event? = null, val options: List<EventBookingOption> = emptyList(), val selectedOptionId: String? = null,
    val quantity: Int = 0, val attendees: List<PendingAttendee> = emptyList(), val validationMessage: String? = null,
    val errorMessage: String? = null, val httpStatus: Int? = null, val holdKey: String? = null, val holdExpiresAt: String? = null
) {
    val selectedOption get() = options.firstOrNull { it.id == selectedOptionId }
    val subtotalPaise get() = (selectedOption?.pricePaise ?: 0) * quantity
}

class EventBookingViewModel(private val pendingCheckoutStore: PendingCheckoutStore) : ViewModel() {
    private val state = MutableStateFlow(EventBookingUiState()); val uiState: StateFlow<EventBookingUiState> = state.asStateFlow(); private var eventId = ""
    fun loadEvent(id: String) {
        eventId = id; val event = FakeData.getEventById(id)
        val options = FakeData.getTicketTiers(id).mapIndexed { index, tier -> EventBookingOption(tier.id, index + 1, tier.name, tier.description, tier.price * 100, tier.available, "INR") }
        state.value = EventBookingUiState(event = event, options = options, selectedOptionId = options.firstOrNull { it.remaining > 0 }?.id, errorMessage = if (event == null) "Event preview is unavailable." else null)
    }
    fun selectOption(optionId: String) { state.value = state.value.copy(selectedOptionId = optionId, quantity = 0, attendees = emptyList(), validationMessage = null) }
    fun setQuantity(quantity: Int) {
        val option = state.value.selectedOption ?: return; val count = quantity.coerceIn(0, minOf(option.remaining, 10))
        state.value = state.value.copy(quantity = count, attendees = List(count) { state.value.attendees.getOrNull(it) ?: PendingAttendee("", "") }, validationMessage = null)
    }
    fun updateAttendee(index: Int, fullName: String? = null, phone: String? = null) {
        val list = state.value.attendees.toMutableList(); val current = list.getOrNull(index) ?: return
        list[index] = current.copy(fullName = fullName ?: current.fullName, phone = phone ?: current.phone)
        state.value = state.value.copy(attendees = list, validationMessage = null)
    }
    fun validateSelection(): Boolean {
        val error = when {
            state.value.quantity == 0 -> "Select at least one ticket."
            state.value.attendees.any { it.fullName.isBlank() || it.phone.length < 7 } -> "Enter a valid name and phone for every attendee."
            else -> null
        }
        state.value = state.value.copy(validationMessage = error); return error == null
    }
    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (!validateSelection()) return; val current = state.value; val event = current.event ?: return; val option = current.selectedOption ?: return
        val bill = previewBill("EVENT", current.quantity, current.subtotalPaise)
        pendingCheckoutStore.save(PendingEventCheckout(event.id, event.title, "preview-event-hold", Instant.now().plusSeconds(300).toString(), option.zoneId, option.name, current.attendees, current.subtotalPaise, "INR", bill))
        onSuccess()
    }
    fun releaseAndGoBack(onReleased: () -> Unit) { pendingCheckoutStore.clear(); onReleased() }
    fun retry() = loadEvent(eventId)
}
