package com.entrymyslot.app.screens.ticket

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingEventCheckout
import com.entrymyslot.app.data.booking.PendingMovieCheckout
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.booking.formatPaiseAsRupees
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
            is PendingEventCheckout -> pending.zoneName
            is PendingTurfCheckout -> pending.formattedTime.substringBefore(" - ").trim()
            else -> existing?.details ?: "1 admission"
        }
        val amount = pending?.bill?.totalPaise?.let(::formatPaiseAsRupees) ?: existing?.price ?: "Confirmed"
        val venue = when (pending) {
            is PendingMovieCheckout -> pending.cinemaName
            is PendingEventCheckout -> item?.location.orEmpty()
            is PendingTurfCheckout -> pending.resourceName
            else -> item?.location ?: existing?.location.orEmpty()
        }
        val location = when (pending) {
            is PendingMovieCheckout -> pending.venueLocation
            is PendingEventCheckout -> pending.venueLocation
            is PendingTurfCheckout -> pending.venueLocation
            else -> existing?.location.orEmpty()
        }
        val date = when (pending) {
            is PendingMovieCheckout -> pending.showDatetime.substringBefore('T')
            is PendingEventCheckout -> pending.eventDate
            is PendingTurfCheckout -> pending.bookingDate
            else -> dateTime.substringBefore(" • ").ifBlank { "Booking date" }
        }
        val time = when (pending) {
            is PendingMovieCheckout -> pending.showDatetime.substringAfter('T', "").take(5)
            is PendingEventCheckout -> pending.eventTime
            is PendingTurfCheckout -> pending.formattedTime.substringBefore(" - ").trim()
            else -> dateTime.substringAfter(" • ", "Confirmed")
        }
        state.value = TicketUiState(
            ticket = TicketDetails(
                bookingId = bookingKey.ifBlank { "EMS-PREVIEW" }, ticketUuid = ticketUuid.takeUnless { it.isBlank() || it == "_" } ?: bookingKey,
                title = item?.title ?: existing?.title ?: "EntryMySlot Ticket", category = type.uppercase(),
                venue = venue, location = location, date = date, time = time, admission = admission,
                amount = amount,
                language = (pending as? PendingMovieCheckout)?.language,
                format = (pending as? PendingMovieCheckout)?.format,
                screenName = (pending as? PendingMovieCheckout)?.screenName,
                seatTier = (pending as? PendingMovieCheckout)?.seatTier,
                ticketCount = when (pending) {
                    is PendingMovieCheckout -> pending.seatLabels.size
                    is PendingEventCheckout -> pending.quantity
                    is PendingTurfCheckout -> 1
                    else -> null
                },
                ticketTier = (pending as? PendingEventCheckout)?.zoneName,
                encodedQrPayload = ticketUuid.takeUnless { it.isBlank() || it == "_" } ?: bookingKey
            )
        )
    }
}
