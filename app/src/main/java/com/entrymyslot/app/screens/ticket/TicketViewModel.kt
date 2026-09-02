package com.entrymyslot.app.screens.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.booking.BookingApi
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingEventCheckout
import com.entrymyslot.app.data.booking.PendingMovieCheckout
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.details.DetailsApi
import com.entrymyslot.app.data.model.TicketDetails
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

data class TicketUiState(
    val isLoading: Boolean = true,
    val ticket: TicketDetails? = null,
    val errorMessage: String? = null
)

class TicketViewModel(
    private val type: String,
    private val itemId: String,
    private val bookingKey: String,
    private val ticketUuid: String,
    private val bookingApi: BookingApi,
    private val detailsApi: DetailsApi,
    private val pendingCheckoutStore: PendingCheckoutStore
) : ViewModel() {

    private val shouldResolveTicketFromServer = ticketUuid.isBlank() || ticketUuid == "_"

    private val _uiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    init {
        loadTicket()
    }

    fun loadTicket() {
        if (_uiState.value.isLoading && _uiState.value.ticket != null) return
        viewModelScope.launch {
            _uiState.value = TicketUiState(isLoading = true)
            try {
                val ticket = when (type.uppercase(Locale.ROOT)) {
                    "MOVIE" -> loadMovieTicket()
                    "EVENT" -> loadEventTicket()
                    "TURF" -> loadTurfTicket()
                    else -> throw IllegalArgumentException("Unsupported ticket type.")
                }
                _uiState.value = TicketUiState(isLoading = false, ticket = ticket)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = TicketUiState(
                    isLoading = false,
                    errorMessage = error.toTicketMessage()
                )
            }
        }
    }

    private suspend fun loadMovieTicket(): TicketDetails {
        val details = bookingApi.getMovieBooking(bookingKey).data
        val ticket = bookingApi.getMovieTickets(bookingKey).data
            .firstOrNull { shouldResolveTicketFromServer || it.ticketUuid == ticketUuid }
            ?: throw IllegalStateException("Ticket not found for this movie booking.")
        val (date, time) = details.showtime.showDatetime.toTicketDateTime()
        return TicketDetails(
            bookingId = details.booking.bookingReference,
            ticketUuid = ticket.ticketUuid,
            title = details.movie.title,
            category = "MOVIE",
            venue = listOf(details.cinema.name, details.cinema.city).filter(String::isNotBlank).joinToString(", "),
            date = date,
            time = time,
            admission = ticket.seatLabel,
            attendee = "Ticket holder",
            amount = "₹${(details.booking.amount.content.toDoubleOrNull() ?: 0.0) / 100.0}".trimMoney()
        )
    }

    private suspend fun loadEventTicket(): TicketDetails {
        val bookingId = bookingKey.toIntOrNull() ?: throw IllegalArgumentException("Invalid event booking id.")
        val booking = bookingApi.getEventBooking(bookingId).data
        val ticket = booking.tickets.firstOrNull { shouldResolveTicketFromServer || it.ticketUuid == ticketUuid }
            ?: throw IllegalStateException("Ticket not found for this event booking.")
        val event = detailsApi.getEventDetails(itemId).data
        val (date, time) = (event.startAt ?: "").toTicketDateTime()
        val pending = pendingCheckoutStore.current.value as? PendingEventCheckout
        val totalPaise = pending?.bill?.totalPaise
            ?: ((event.price.toDoubleOrNull() ?: 0.0) * 100 * booking.booking.ticketCount).toInt()
        return TicketDetails(
            bookingId = "EVT-${booking.booking.id}",
            ticketUuid = ticket.ticketUuid,
            title = event.title,
            category = "EVENT",
            venue = listOf(event.venue, event.city.orEmpty()).filter(String::isNotBlank).distinct().joinToString(", "),
            date = date,
            time = time,
            admission = pending?.zoneName?.takeIf(String::isNotBlank) ?: "General admission",
            attendee = ticket.attendeeName,
            amount = "₹${totalPaise / 100.0}".trimMoney()
        )
    }

    private suspend fun loadTurfTicket(): TicketDetails {
        val bookingId = bookingKey.toIntOrNull() ?: throw IllegalArgumentException("Invalid turf booking id.")
        val booking = bookingApi.getTurfBooking(bookingId).data
        val serverTicketUuid = booking.qrToken
            ?: throw IllegalStateException("Server did not return a ticket for this turf booking.")
        if (!shouldResolveTicketFromServer && serverTicketUuid != ticketUuid) {
            throw IllegalStateException("Ticket does not match this turf booking.")
        }
        val pending = pendingCheckoutStore.current.value as? PendingTurfCheckout
        val (date, time) = booking.slotStart.toTicketDateTime()
        val endTime = booking.slotEnd.toTicketDateTime().second
        return TicketDetails(
            bookingId = booking.bookingReference,
            ticketUuid = serverTicketUuid,
            title = booking.resourceName.ifBlank { pending?.resourceName.orEmpty() },
            category = "TURF",
            venue = booking.venueName.ifBlank { pending?.resourceName.orEmpty() },
            date = date,
            time = listOf(time, endTime).filter(String::isNotBlank).joinToString(" - "),
            admission = "1 slot",
            attendee = booking.customerName ?: "Ticket holder",
            amount = "₹${booking.amount.content.toDoubleOrNull() ?: 0.0}".trimMoney()
        )
    }
}

private fun String.toTicketDateTime(): Pair<String, String> = runCatching {
    val value = OffsetDateTime.parse(this)
    val date = value.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
    val time = value.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
    date to time
}.getOrElse {
    substringBefore('T').ifBlank { "Date unavailable" } to
        substringAfter('T', "").take(5).ifBlank { "Time unavailable" }
}

private fun String.trimMoney(): String {
    val value = removePrefix("₹").toDoubleOrNull() ?: return this
    return if (value % 1.0 == 0.0) "₹${value.toInt()}" else "₹${"%.2f".format(Locale.ENGLISH, value)}"
}

private fun Throwable.toTicketMessage(): String {
    if (this is HttpException) {
        val backendMessage = response()?.errorBody()?.string()?.let { body ->
            runCatching { org.json.JSONObject(body).optString("message") }.getOrNull()
        }
        return backendMessage?.takeIf(String::isNotBlank)
            ?: "Unable to load the server ticket (${code()})."
    }
    val text = message.orEmpty()
    return when {
        text.contains("Unable to resolve host", true) || text.contains("failed to connect", true) ->
            "No internet connection. Reconnect and try again."
        text.isNotBlank() -> text
        else -> "Unable to load this ticket."
    }
}
