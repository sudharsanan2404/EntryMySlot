package com.entrymyslot.app.screens.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.model.TicketDetails
import com.entrymyslot.app.data.mapper.paiseLabel
import com.entrymyslotbe.app.EntryMySlotBackend
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TicketUiState(val isLoading: Boolean = false, val ticket: TicketDetails? = null, val errorMessage: String? = null)
class TicketViewModel(
    private val type: String, private val itemId: String, private val bookingKey: String,
    private val ticketUuid: String, private val backend: EntryMySlotBackend, private val pendingCheckoutStore: PendingCheckoutStore
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
                    val id = ticketUuid.takeUnless { it.isBlank() || it == "_" } ?: when (val result = backend.gateway.data { getMyTickets(bookingKey) }) {
                        is ApiResult.Success -> result.value?.firstOrNull()?.ticketUuid
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("No issued ticket is available for this booking.")
                    val ticket = when (val result = backend.gateway.data { getTicketDetails(id) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("Ticket details are unavailable.")
                    if (ticket.status in setOf("revoked", "invalid", "cancelled")) return@launch fail("This ticket is no longer valid.")
                    val date = ticket.showtimeDatetime ?: booking.showDatetime.orEmpty()
                    state.value = TicketUiState(ticket = TicketDetails(booking.bookingReference ?: bookingKey, ticket.ticketUuid ?: id,
                        ticket.movieTitle ?: booking.movieTitle.orEmpty(), "MOVIE", ticket.cinemaName ?: booking.cinemaName.orEmpty(),
                        date.substringBefore('T'), date.substringAfter('T', "").take(5), ticket.seatLabel.orEmpty(), booking.customerName.orEmpty(),
                        booking.totalAmount.paiseLabel(), ticket.qrData))
                }
                "EVENT" -> {
                    val details = when (val result = backend.gateway.data { getEventBookingDetails(bookingKey) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("Booking details are unavailable.")
                    val booking = details.booking ?: return@launch fail("Booking is unavailable.")
                    if (booking.status !in setOf("confirmed", "completed")) return@launch fail("This booking does not have a confirmed ticket.")
                    val ticket = details.tickets.firstOrNull { it.ticketUuid == ticketUuid } ?: details.tickets.firstOrNull()
                        ?: return@launch fail("No issued ticket is available for this booking.")
                    val id = ticket.ticketUuid ?: return@launch fail("Ticket identity is unavailable.")
                    val event = when (val result = backend.gateway.data { getEvent(booking.eventId ?: itemId) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    }
                    state.value = TicketUiState(ticket = TicketDetails(booking.bookingReference ?: bookingKey, id,
                        event?.title ?: booking.eventTitle.orEmpty(), "EVENT", event?.venue.orEmpty(),
                        event?.startDate.orEmpty().substringBefore('T'), event?.startTime?.take(5).orEmpty(),
                        booking.ticketCount?.let { "$it tickets" }.orEmpty(), ticket.attendeeName ?: booking.attendeeName.orEmpty(), booking.totalAmount.paiseLabel(),
                        // Event PDFs encode the server-issued UUID directly (server pdfService).
                        ticket.qrData ?: id))
                }
                "TURF" -> {
                    val booking = when (val result = backend.gateway.data { getTurfBooking(bookingKey) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("Booking details are unavailable.")
                    if (booking.status !in setOf("confirmed", "completed", "checked_in")) return@launch fail("This booking does not have a confirmed ticket.")
                    val ticketId = booking.qrToken ?: return@launch fail("The server has not issued a turf ticket.")
                    val qr = booking.qrData ?: ticketId
                    state.value = TicketUiState(ticket = TicketDetails(booking.bookingReference ?: bookingKey, ticketId,
                        booking.resourceName ?: booking.venueName.orEmpty(), "TURF", booking.venueName.orEmpty(),
                        booking.date ?: booking.startsAt.orEmpty().substringBefore('T'), booking.timeSlot ?: listOfNotNull(booking.startsAt, booking.endsAt).joinToString(" - "), "",
                        booking.customerName.orEmpty(), booking.totalAmount?.toPlainString()?.let { "₹$it" }.orEmpty(), qr))
                }
                else -> fail("Unsupported booking category.")
            }
        }
    }
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
