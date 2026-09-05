package com.entrymyslot.app.screens.turf

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.booking.TurfSlotDto
import com.entrymyslot.app.data.model.Turf
import com.entrymyslot.app.screens.movies.previewBill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate

data class TurfBookingUiState(
    val isLoading: Boolean = false, val isHolding: Boolean = false, val isOffline: Boolean = false,
    val turf: Turf? = null, val selectedDate: LocalDate = LocalDate.now(), val slots: List<TurfSlotDto> = emptyList(),
    val selectedUnitId: Int? = null, val holdToken: String? = null, val holdExpiresAt: String? = null,
    val holdSecondsRemaining: Int = 0, val errorMessage: String? = null, val httpStatus: Int? = null
) { val selectedSlot get() = slots.firstOrNull { it.unit_id == selectedUnitId } }

class TurfBookingViewModel(private val pendingCheckoutStore: PendingCheckoutStore) : ViewModel() {
    private val state = MutableStateFlow(TurfBookingUiState()); val uiState: StateFlow<TurfBookingUiState> = state.asStateFlow(); private var resourceId = ""
    fun loadTurf(id: String, date: LocalDate = state.value.selectedDate) {
        resourceId = id; val turf = FakeData.getTurfById(id)
        val slots = (6..22).map { hour -> TurfSlotDto(hour, "${date}T${hour.toString().padStart(2, '0')}:00:00Z", "${date}T${(hour + 1).coerceAtMost(23).toString().padStart(2, '0')}:00:00Z", if (hour in setOf(9, 14, 19)) "booked" else "available", turf?.pricePerHour?.toDouble(), formatted_time = "%02d:00 - %02d:00".format(hour, hour + 1), duration_minutes = 60) }
        state.value = TurfBookingUiState(turf = turf, selectedDate = date, slots = slots, errorMessage = if (turf == null) "Turf preview is unavailable." else null)
    }
    fun changeDate(date: LocalDate) = loadTurf(resourceId, date)
    fun onSlotClicked(slot: TurfSlotDto) {
        if (slot.status != "available") return
        state.value = state.value.copy(selectedUnitId = if (state.value.selectedUnitId == slot.unit_id) null else slot.unit_id, errorMessage = null)
    }
    fun validateSelection(): Boolean { val valid = state.value.selectedSlot != null; if (!valid) state.value = state.value.copy(errorMessage = "Select a slot before continuing."); return valid }
    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (!validateSelection()) return; val current = state.value; val turf = current.turf ?: return; val slot = current.selectedSlot ?: return
        val subtotal = ((slot.price ?: turf.pricePerHour.toDouble()) * 100).toInt(); val bill = previewBill("TURF", 1, subtotal)
        pendingCheckoutStore.save(PendingTurfCheckout(turf.id, turf.title, slot.unit_id, slot.starts_at, slot.ends_at, slot.formatted_time, "preview-turf-hold", Instant.now().plusSeconds(300).toString(), subtotal, "INR", bill))
        onSuccess()
    }
    fun releaseAndGoBack(onReleased: () -> Unit) { pendingCheckoutStore.clear(); onReleased() }
    fun retry() = loadTurf(resourceId, state.value.selectedDate)
}
