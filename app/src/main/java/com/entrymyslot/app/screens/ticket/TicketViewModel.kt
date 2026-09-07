package com.entrymyslot.app.screens.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.model.TicketDetails
import com.entrymyslot.app.data.mapper.paiseLabel
import com.entrymyslot.app.data.mapper.ticketDateTime
import com.entrymyslotbe.app.EntryMySlotBackend
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TicketUiState(
    val isLoading: Boolean = false, val tickets: List<TicketDetails> = emptyList(),
    val selectedTicketIndex: Int = 0, val errorMessage: String? = null
) {
    val ticket: TicketDetails? get() = tickets.getOrNull(selectedTicketIndex)
    fun selectTicket(index: Int): TicketUiState = if (index in tickets.indices) copy(selectedTicketIndex = index) else this
}
class TicketViewModel(
    private val type: String, private val itemId: String, private val bookingKey: String,
    private val ticketUuid: String, private val backend: EntryMySlotBackend
) : ViewModel() {
    private val state = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = state.asStateFlow()
    init { loadTicket() }
    fun loadTicket() {
        if (state.value.isLoading) return
        state.value = TicketUiState(isLoading = true)
        viewModelScope.launch {
            when (type.uppercase()) {
                "MOVIE" -> {
                    val response = when (val result = backend.gateway.data { getBooking(bookingKey) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("Booking details are unavailable.")
                    val booking = response.booking ?: response
                    if (booking.status !in setOf("confirmed", "completed")) return@launch fail("This booking does not have a confirmed ticket.")
                    val issuedTickets = when (val result = backend.gateway.data { getMyTickets(bookingKey) }) {
                        is ApiResult.Success -> result.value.orEmpty()
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    }
                    val ids = issuedTickets.mapNotNull { it.ticketUuid?.takeIf(String::isNotBlank) }.distinct()
                    if (ids.isEmpty()) return@launch fail("No issued ticket is available for this booking.")
                    val results = ids.map { id -> async { backend.gateway.data { getTicketDetails(id) } } }.awaitAll()
                    val failure = results.filterIsInstance<ApiResult.Failure>().firstOrNull()
                    if (failure != null) return@launch fail(failure.userMessage)
                    val tickets = results.mapIndexedNotNull { index, result ->
                        val ticket = (result as? ApiResult.Success)?.value ?: return@mapIndexedNotNull null
                        if (ticket.status in setOf("revoked", "invalid", "cancelled", "canceled")) return@mapIndexedNotNull null
                        val (date, time) = ticketDateTime(ticket.showtimeDatetime ?: booking.showDatetime)
                        TicketDetails(booking.bookingReference ?: bookingKey, ticket.ticketUuid ?: ids[index],
                            ticket.movieTitle ?: booking.movieTitle.orEmpty(), "MOVIE", ticket.cinemaName ?: booking.cinemaName.orEmpty(),
                            date, time, ticket.seatLabel.orEmpty(), booking.customerName.orEmpty(),
                            booking.totalAmount.paiseLabel(), ticket.qrData?.takeIf(String::isNotBlank))
                    }
                    showTickets(tickets)
                }
                "EVENT" -> {
                    val details = when (val result = backend.gateway.data { getEventBookingDetails(bookingKey) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("Booking details are unavailable.")
                    val booking = details.booking ?: return@launch fail("Booking is unavailable.")
                    if (booking.status !in setOf("confirmed", "completed")) return@launch fail("This booking does not have a confirmed ticket.")
                    val event = when (val result = backend.gateway.data { getEvent(booking.eventId ?: itemId) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    }
                    val (date, time) = ticketDateTime(event?.startDate ?: booking.eventDate)
                    showTickets(details.tickets.mapNotNull { ticket ->
                        val id = ticket.ticketUuid?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                        if (ticket.status in setOf("revoked", "invalid", "cancelled", "canceled")) return@mapNotNull null
                        TicketDetails(booking.bookingReference ?: bookingKey, id,
                            event?.title ?: booking.eventTitle.orEmpty(), "EVENT", event?.venue ?: booking.venue.orEmpty(),
                            date, event?.startTime?.takeIf(String::isNotBlank)?.take(5) ?: time,
                            "1 admission", ticket.attendeeName ?: booking.attendeeName.orEmpty(), booking.totalAmount.paiseLabel(),
                            // Event PDFs encode the server-issued UUID directly (server pdfService).
                            ticket.qrData?.takeIf(String::isNotBlank) ?: id)
                    }.distinctBy { it.ticketUuid })
                }
                "TURF" -> {
                    val booking = when (val result = backend.gateway.data { getTurfBooking(bookingKey) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("Booking details are unavailable.")
                    if (booking.status !in setOf("confirmed", "completed", "checked_in")) return@launch fail("This booking does not have a confirmed ticket.")
                    val ticketId = booking.qrToken?.takeIf(String::isNotBlank) ?: return@launch fail("The server has not issued a turf ticket.")
                    val qr = booking.qrData?.takeIf(String::isNotBlank) ?: ticketId
                    val (date, startTime) = ticketDateTime(booking.startsAt)
                    val endTime = ticketDateTime(booking.endsAt).second
                    showTickets(listOf(TicketDetails(booking.bookingReference ?: bookingKey, ticketId,
                        booking.resourceName ?: booking.venueName.orEmpty(), "TURF", booking.venueName.orEmpty(),
                        booking.date ?: date, booking.timeSlot?.takeIf(String::isNotBlank) ?: listOf(startTime, endTime).filter(String::isNotBlank).joinToString(" - "), "",
                        booking.customerName.orEmpty(), booking.totalAmount?.toPlainString()?.let { "₹$it" }.orEmpty(), qr)))
                }
                else -> fail("Unsupported booking category.")
            }
        }
    }
    private fun showTickets(tickets: List<TicketDetails>) {
        if (tickets.isEmpty()) return fail("No valid issued tickets are available for this booking.")
        state.value = TicketUiState(tickets = tickets, selectedTicketIndex = tickets.indexOfFirst { it.ticketUuid == ticketUuid }.coerceAtLeast(0))
    }
    fun previousTicket() { state.value = state.value.selectTicket(state.value.selectedTicketIndex - 1) }
    fun nextTicket() { state.value = state.value.selectTicket(state.value.selectedTicketIndex + 1) }
    private fun fail(message: String) { state.value = TicketUiState(errorMessage = message) }

    fun savePdf(context: android.content.Context, destination: android.net.Uri) {
        val ticket = state.value.ticket ?: return
        viewModelScope.launch {
            val body = if (type.equals("EVENT", true)) {
                when (val result = backend.gateway.execute { getEventBookingPdf(bookingKey) }) {
                    is ApiResult.Success -> result.value
                    is ApiResult.Failure -> {
                        state.value = state.value.copy(errorMessage = result.userMessage)
                        return@launch
                    }
                }
            } else null
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    context.contentResolver.openOutputStream(destination)?.use { output ->
                        if (body != null) body.use { it.byteStream().copyTo(output) }
                        else {
                            val document = android.graphics.pdf.PdfDocument()
                            try {
                            val page = document.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create())
                            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textSize = 14f }
                            val lines = listOf(ticket.title, ticket.category, ticket.venue, ticket.date, ticket.time,
                                ticket.admission, ticket.attendee, ticket.amount, ticket.bookingId)
                            lines.forEachIndexed { index, line -> page.canvas.drawText(line, 40f, 55f + index * 26f, paint) }
                            val matrix = com.google.zxing.qrcode.QRCodeWriter().encode(ticket.qrPayload, com.google.zxing.BarcodeFormat.QR_CODE, 240, 240)
                            val bitmap = android.graphics.Bitmap.createBitmap(240, 240, android.graphics.Bitmap.Config.ARGB_8888)
                            for (x in 0 until 240) for (y in 0 until 240) bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                            page.canvas.drawBitmap(bitmap, 40f, 330f, paint)
                            document.finishPage(page)
                            document.writeTo(output)
                            bitmap.recycle()
                            } finally { document.close() }
                        }
                    } ?: throw java.io.IOException("Cannot open the selected destination.")
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                state.value = state.value.copy(errorMessage = "Unable to save the ticket PDF.")
            } finally { body?.close() }
        }
    }
}
