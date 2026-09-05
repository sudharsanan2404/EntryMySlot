package com.entrymyslot.app.screens.ticket

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingEventCheckout
import com.entrymyslot.app.data.booking.PendingMovieCheckout
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.model.TicketDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TicketUiState(val isLoading: Boolean = false, val ticket: TicketDetails? = null, val errorMessage: String? = null)

class TicketViewModel(
    private val type: String, private val itemId: String, private val bookingKey: String,
    private val ticketUuid: String, private val pendingCheckoutStore: PendingCheckoutStore
) : ViewModel() {
    private val state = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = state.asStateFlow()
    init { loadTicket() }
    fun loadTicket() {
        val pending = pendingCheckoutStore.current.value
        val existing = FakeData.bookings.firstOrNull { it.id == bookingKey || it.bookingReference == bookingKey || it.itemId == itemId }
        val item = FakeData.getItemById(itemId)
        val dateTime = existing?.dateTime.orEmpty()
        val admission = when (pending) {
            is PendingMovieCheckout -> pending.seatLabels.joinToString(", ").ifBlank { "Selected seats" }
            is PendingEventCheckout -> "${pending.attendees.size} × ${pending.zoneName}"
            is PendingTurfCheckout -> pending.formattedTime
            else -> existing?.details ?: "1 admission"
        }
        val amount = pending?.bill?.totalPaise?.let { "₹${it / 100}" } ?: existing?.price ?: "Confirmed"
        state.value = TicketUiState(
            ticket = TicketDetails(
                bookingId = bookingKey.ifBlank { "EMS-PREVIEW" }, ticketUuid = ticketUuid.takeUnless { it.isBlank() || it == "_" } ?: bookingKey,
                title = item?.title ?: existing?.title ?: "EntryMySlot Ticket", category = type.uppercase(),
                venue = item?.location ?: existing?.location.orEmpty(),
                date = dateTime.substringBefore(" • ").ifBlank { "Booking date" },
                time = dateTime.substringAfter(" • ", "Confirmed"), admission = admission,
                attendee = FakeData.currentUser.fullName, amount = amount
            )
        )
    }
}
