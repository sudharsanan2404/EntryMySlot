package com.entrymyslot.app.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.booking.*
import com.entrymyslotbe.app.EntryMySlotBackend
import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.network.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class ConfirmedBookingRoute(val type: String, val itemId: String, val bookingKey: String, val ticketUuid: String)
data class PaymentUiState(
    val isProcessing: Boolean = false, val errorMessage: String? = null, val confirmedBooking: ConfirmedBookingRoute? = null,
    val amountPaise: Int? = null, val checkoutUrl: String? = null, val isReady: Boolean = false
)
class PaymentViewModel(private val backend: EntryMySlotBackend, private val pendingCheckoutStore: PendingCheckoutStore) : ViewModel() {
    private val state = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = state.asStateFlow()
    private var checkoutLaunched = false
    init { preparePayment() }

    fun preparePayment() {
        if (state.value.isProcessing || state.value.confirmedBooking != null) return
        val saved = pendingCheckoutStore.payment
        if (saved != null) {
            if (saved.type == "TURF" && saved.orderId.isNullOrBlank()) {
                state.value = state.value.copy(isProcessing = true, errorMessage = null)
                viewModelScope.launch { createTurfOrder(saved) }
            } else ready(saved)
            return
        }
        val checkout = pendingCheckoutStore.current.value ?: return fail("Booking selection expired. Please select again.")
        if (pendingCheckoutStore.creationAttempted) return fail("The booking request may already have reached the server. Check My Bookings before starting another booking.")
        state.value = state.value.copy(isProcessing = true, errorMessage = null)
        viewModelScope.launch {
            when (checkout) {
                is PendingMovieCheckout -> {
                    val user = checkoutUser() ?: return@launch
                    val hold = when (val result = backend.gateway.data { checkHold(checkout.holdKey) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    }
                    if (hold?.active != true || hold.heldSeatIds.orEmpty().toSet() != checkout.seatIds.map(Int::toString).toSet())
                        return@launch fail("Seat hold expired or changed. Select your seats again.")
                    pendingCheckoutStore.markCreationAttempted()
                    val response = when (val result = backend.gateway.data { createMovieBooking(MovieBookingRequest(checkout.holdKey,
                        user.email.orEmpty(), user.phone.orEmpty(), user.name ?: user.username.orEmpty(), idempotencyKey = checkout.holdKey)) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("The server returned no booking.")
                    val booking = response.booking ?: return@launch fail("The server returned no booking details.")
                    val key = booking.id ?: return@launch fail("The server returned no booking ID.")
                    val reference = booking.bookingReference ?: return@launch fail("The server returned no booking reference.")
                    val payment = PendingPayment("MOVIE", checkout.itemId, key, reference, response.paymentOrderId,
                        booking.totalAmount?.let { runCatching { it.intValueExact() }.getOrNull() }, response.paymentUrl, response.paymentSessionId)
                    pendingCheckoutStore.savePayment(payment)
                    ready(payment)
                }
                is PendingEventCheckout -> {
                    pendingCheckoutStore.markCreationAttempted()
                    val response = when (val result = backend.gateway.data { createEventBooking(EventBookingRequest(checkout.itemId,
                        checkout.attendees.map { EventAttendee(it.fullName.trim(), it.phone.trim()) }, checkout.zoneId, checkout.ticketTierId)) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("The server returned no booking.")
                    val id = response.bookingId ?: return@launch fail("The server returned no booking ID.")
                    val payment = PendingPayment("EVENT", checkout.itemId, id, id, response.payment?.orderId,
                        response.payment?.amount?.takeIf { it in 0..Int.MAX_VALUE.toLong() }?.toInt(), null, response.payment?.paymentSessionId)
                    pendingCheckoutStore.savePayment(payment)
                    ready(payment)
                    if (response.status == "confirmed") verified(payment, response.tickets.firstOrNull()?.ticketUuid.orEmpty())
                }
                is PendingTurfCheckout -> {
                    pendingCheckoutStore.markCreationAttempted()
                    val response = when (val result = backend.gateway.data { createTurfBooking(TurfBookingRequest(checkout.unitId.toLong())) }) {
                        is ApiResult.Success -> result.value?.booking
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    } ?: return@launch fail("The server returned no booking.")
                    val id = response.id ?: return@launch fail("The server returned no booking ID.")
                    val amount = response.totalAmount?.let { runCatching { it.movePointRight(2).intValueExact() }.getOrNull() }?.takeIf { it >= 0 }
                        ?: return@launch fail("The server returned no payable amount.")
                    val draft = PendingPayment("TURF", checkout.itemId, id, response.bookingReference ?: id, null, amount, null, null)
                    pendingCheckoutStore.savePayment(draft)
                    createTurfOrder(draft)
                }
            }
        }
    }
    private suspend fun createTurfOrder(draft: PendingPayment) {
        val user = checkoutUser() ?: return
        val amount = draft.amountPaise ?: return fail("The server returned no payable amount.")
        val response = when (val result = backend.gateway.data { createPaymentOrder(PaymentOrderRequest(draft.bookingId, "turf", amount,
            user.email.orEmpty(), user.phone.orEmpty())) }) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> return fail(result.userMessage)
        } ?: return fail("The server returned no payment order.")
        val order = response.order ?: response
        if (order.orderId.isNullOrBlank()) return fail("The server returned no payment order ID.")
        val payment = draft.copy(orderId = order.orderId, paymentUrl = response.paymentUrl ?: order.paymentUrl,
            paymentSessionId = response.paymentSessionId ?: order.paymentSessionId)
        pendingCheckoutStore.savePayment(payment)
        ready(payment)
    }
    private suspend fun checkoutUser(): User? {
        val expectedSession = backend.sessions.snapshot(AuthScope.USER)
        if (expectedSession.accessToken.isNullOrBlank()) {
            fail("Your session expired. Please sign in again.")
            return null
        }
        backend.sessions.user()?.let { cached ->
            val currentSession = backend.sessions.snapshot(AuthScope.USER)
            if (currentSession.generation == expectedSession.generation && !currentSession.accessToken.isNullOrBlank()) return cached
            fail("Your session changed. Please sign in again.")
            return null
        }
        return when (val result = backend.gateway.data { getMe() }) {
            is ApiResult.Success -> {
                val user = result.value
                when {
                    user == null -> { fail("Profile is unavailable. Please try again."); null }
                    !backend.sessions.saveUserIfCurrent(expectedSession, user) -> {
                        fail("Your session changed. Please sign in again.")
                        null
                    }
                    else -> user
                }
            }
            is ApiResult.Failure -> { fail(result.userMessage); null }
        }
    }
    private fun ready(payment: PendingPayment) {
        state.value = PaymentUiState(amountPaise = payment.amountPaise, isReady = true,
            errorMessage = if (payment.paymentUrl.isNullOrBlank()) "The payment provider has not supplied a checkout link. Payment is not confirmed." else null)
    }
    fun pay() {
        if (state.value.isProcessing || state.value.confirmedBooking != null || state.value.checkoutUrl != null) return
        val payment = pendingCheckoutStore.payment ?: return preparePayment()
        if (payment.type == "TURF" && payment.orderId.isNullOrBlank()) return preparePayment()
        val url = payment.paymentUrl
        if (url != null && runCatching { java.net.URI(url).scheme == "https" }.getOrDefault(false))
            state.value = state.value.copy(checkoutUrl = url, errorMessage = null)
        else verifyPayment()
    }
    fun checkoutOpened(opened: Boolean = true) {
        checkoutLaunched = opened
        state.value = state.value.copy(checkoutUrl = null)
    }
    fun onResume() {
        if (!checkoutLaunched) return
        checkoutLaunched = false
        verifyPayment()
    }
    fun verifyPayment(gatewayPaymentId: String? = null) {
        if (state.value.isProcessing || state.value.confirmedBooking != null) return
        val payment = pendingCheckoutStore.payment ?: return
        state.value = state.value.copy(isProcessing = true, errorMessage = null)
        viewModelScope.launch {
            when (payment.type) {
                "MOVIE" -> {
                    val response = when (val result = backend.gateway.data { confirmBooking(buildMap {
                        put("bookingReference", payment.bookingReference)
                        payment.orderId?.let { put("paymentOrderId", it) }
                    }) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    }
                    val booking = response?.booking ?: response
                    if (booking?.status != "confirmed") return@launch fail("Payment has not been confirmed by the server.")
                    val tickets = when (val result = backend.gateway.data { getMyTickets(payment.bookingReference) }) {
                        is ApiResult.Success -> result.value.orEmpty()
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    }
                    verified(payment, tickets.firstOrNull()?.ticketUuid.orEmpty())
                }
                "EVENT" -> {
                    val response = when (val result = backend.gateway.data { verifyEventBookingPayment(payment.bookingId,
                        "{}".toRequestBody("application/json".toMediaType())) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    }
                    if (response?.status != "confirmed") return@launch fail("Payment has not been confirmed by the server.")
                    verified(payment, response.tickets?.firstOrNull()?.ticketUuid.orEmpty())
                }
                "TURF" -> {
                    if (!gatewayPaymentId.isNullOrBlank() && !payment.orderId.isNullOrBlank()) {
                        val result = backend.gateway.data { verifyPayment(PaymentVerifyRequest(payment.bookingId, payment.orderId, gatewayPaymentId)) }
                        if (result is ApiResult.Failure) return@launch fail(result.userMessage)
                    }
                    val booking = when (val result = backend.gateway.data { getTurfBooking(payment.bookingId) }) {
                        is ApiResult.Success -> result.value
                        is ApiResult.Failure -> return@launch fail(result.userMessage)
                    }
                    if (booking?.status != "confirmed") return@launch fail("Payment is awaiting provider confirmation. A real provider payment ID is required to verify this booking.")
                    verified(payment, "")
                }
            }
        }
    }
    private fun verified(payment: PendingPayment, ticket: String) {
        state.value = state.value.copy(isProcessing = false, errorMessage = null,
            confirmedBooking = ConfirmedBookingRoute(payment.type, payment.itemId,
                if (payment.type == "MOVIE") payment.bookingReference else payment.bookingId, ticket.ifBlank { "_" }))
    }
    private fun fail(message: String) { state.value = state.value.copy(isProcessing = false, errorMessage = message) }
    fun clearError() { state.value = state.value.copy(errorMessage = null) }
}
