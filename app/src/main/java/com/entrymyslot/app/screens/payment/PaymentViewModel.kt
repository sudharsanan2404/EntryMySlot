package com.entrymyslot.app.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.booking.BookingApi
import com.entrymyslot.app.data.booking.EventBookingAttendeeRequest
import com.entrymyslot.app.data.booking.EventBookingCreateRequest
import com.entrymyslot.app.data.booking.MovieBookingConfirmRequest
import com.entrymyslot.app.data.booking.MovieBookingCreateRequest
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingEventCheckout
import com.entrymyslot.app.data.booking.PendingMovieCheckout
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.booking.TurfBookingCreateRequest
import com.entrymyslot.app.data.booking.TurfPaymentCreateRequest
import com.entrymyslot.app.data.booking.TurfPaymentVerifyRequest
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ConfirmedBookingRoute(
    val type: String,
    val itemId: String,
    val bookingKey: String,
    val ticketUuid: String
)

data class PaymentUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val confirmedBooking: ConfirmedBookingRoute? = null
)

class PaymentViewModel(
    private val bookingApi: BookingApi,
    private val pendingCheckoutStore: PendingCheckoutStore,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val idempotencyKey = UUID.randomUUID().toString()

    fun completeFakePayment() {
        if (_uiState.value.isProcessing || _uiState.value.confirmedBooking != null) return
        val checkout = pendingCheckoutStore.current.value
        if (checkout == null) {
            _uiState.value = PaymentUiState(errorMessage = "Booking selection expired. Please select again.")
            return
        }
        if (!networkMonitor.isCurrentlyOnline()) {
            _uiState.value = PaymentUiState(errorMessage = "No internet connection. Reconnect and try again.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PaymentUiState(isProcessing = true)
            try {
                val confirmed = when (checkout) {
                    is PendingMovieCheckout -> confirmMovie(checkout)
                    is PendingEventCheckout -> confirmEvent(checkout)
                    is PendingTurfCheckout -> confirmTurf(checkout)
                }
                _uiState.value = PaymentUiState(confirmedBooking = confirmed)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = PaymentUiState(errorMessage = error.toPaymentMessage())
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun confirmMovie(checkout: PendingMovieCheckout): ConfirmedBookingRoute {
        val created = bookingApi.createMovieBooking(
            MovieBookingCreateRequest(
                holdKey = checkout.holdKey,
                idempotencyKey = idempotencyKey
            )
        ).data
        val reference = created.booking.bookingReference
        bookingApi.confirmMovieBooking(
            MovieBookingConfirmRequest(
                bookingReference = reference,
                paymentOrderId = created.paymentOrderId
            )
        )
        val ticket = bookingApi.getMovieTickets(reference).data.firstOrNull()
            ?: throw IllegalStateException("Server confirmed the booking but did not return a ticket.")
        return ConfirmedBookingRoute("MOVIE", checkout.itemId, reference, ticket.ticketUuid)
    }

    private suspend fun confirmEvent(checkout: PendingEventCheckout): ConfirmedBookingRoute {
        val created = bookingApi.createEventBooking(
            EventBookingCreateRequest(
                eventId = checkout.itemId.toIntOrNull()
                    ?: throw IllegalStateException("Invalid event id."),
                attendees = checkout.attendees.map {
                    EventBookingAttendeeRequest(it.fullName, it.phone)
                },
                zoneId = checkout.zoneId,
                holdKey = checkout.holdKey
            )
        ).data
        if (created.status != "confirmed") {
            val verified = bookingApi.verifyEventBooking(created.bookingId).data
            if (verified.status != "confirmed") {
                throw IllegalStateException(verified.message.ifBlank { "Event payment was not confirmed." })
            }
        }
        val ticketUuid = created.tickets.firstOrNull()?.ticketUuid
            ?: bookingApi.getEventBooking(created.bookingId).data.tickets.firstOrNull()?.ticketUuid
            ?: throw IllegalStateException("Server confirmed the booking but did not return a ticket.")
        return ConfirmedBookingRoute("EVENT", checkout.itemId, created.bookingId.toString(), ticketUuid)
    }

    private suspend fun confirmTurf(checkout: PendingTurfCheckout): ConfirmedBookingRoute {
        val created = bookingApi.createTurfBooking(
            TurfBookingCreateRequest(
                availabilityUnitId = checkout.unitId,
                holdToken = checkout.holdToken
            )
        ).data.booking
        val paymentOrder = bookingApi.createTurfPaymentOrder(
            TurfPaymentCreateRequest(created.id)
        ).data.order
        val verified = bookingApi.verifyTurfPayment(
            TurfPaymentVerifyRequest(created.id, paymentOrder.orderId)
        ).data
        if (verified.status != "confirmed") {
            throw IllegalStateException("Turf payment was not confirmed.")
        }
        val ticketUuid = verified.booking.qrToken
            ?: bookingApi.getTurfBooking(created.id).data.qrToken
            ?: throw IllegalStateException("Server confirmed the booking but did not return a ticket.")
        return ConfirmedBookingRoute("TURF", checkout.itemId, created.id.toString(), ticketUuid)
    }
}

private fun Throwable.toPaymentMessage(): String {
    if (this is HttpException) {
        val backendMessage = response()?.errorBody()?.string()?.let { body ->
            runCatching { org.json.JSONObject(body).optString("message") }.getOrNull()
        }
        if (!backendMessage.isNullOrBlank()) return backendMessage
        return when (code()) {
            401 -> "Your session expired. Please sign in again."
            409 -> "Your selected hold expired or is no longer available. Please select again."
            429 -> "Too many attempts. Please wait and try again."
            else -> "Server could not complete this booking (${code()})."
        }
    }
    val message = message.orEmpty()
    return when {
        message.contains("Unable to resolve host", true) ||
            message.contains("failed to connect", true) ||
            message.contains("timeout", true) -> "Unable to reach the server. Check your internet and try again."
        message.isNotBlank() -> message
        else -> "Booking failed. Please try again."
    }
}

