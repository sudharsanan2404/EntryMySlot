package com.entrymyslot.app.data.booking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PendingCheckout {
    val itemId: String
    val currency: String
    val bill: AuthoritativeBillDto
}

data class PendingMovieCheckout(
    override val itemId: String,
    val movieTitle: String,
    val showtimeId: Int,
    val cinemaId: Int,
    val cinemaName: String,
    val showDatetime: String,
    val seatIds: List<Int>,
    val seatLabels: List<String>,
    val language: String,
    val format: String,
    val screenName: String,
    val seatTier: String,
    val venueLocation: String,
    val holdKey: String,
    val holdExpiresAt: String,
    val totalPaise: Int,
    override val currency: String,
    override val bill: AuthoritativeBillDto
) : PendingCheckout

data class PendingEventCheckout(
    override val itemId: String,
    val title: String,
    val holdKey: String,
    val holdExpiresAt: String,
    val zoneId: String?,
    val zoneName: String,
    val venueLocation: String,
    val eventDate: String,
    val eventTime: String,
    val quantity: Int,
    val attendees: List<PendingAttendee> = emptyList(),
    val subtotalPaise: Int,
    override val currency: String,
    override val bill: AuthoritativeBillDto,
    val ticketTierId: String? = null
) : PendingCheckout

data class PendingAttendee(
    val fullName: String,
    val phone: String
)

data class PendingTurfCheckout(
    override val itemId: String,
    val resourceName: String,
    val unitId: Int,
    val startsAt: String,
    val endsAt: String,
    val formattedTime: String,
    val venueLocation: String,
    val bookingDate: String,
    val holdToken: String,
    val holdExpiresAt: String,
    val subtotalPaise: Int,
    override val currency: String,
    override val bill: AuthoritativeBillDto
) : PendingCheckout

class PendingCheckoutStore {
    var payment: PendingPayment? = null
        private set
    private val _current = MutableStateFlow<PendingCheckout?>(null)
    val current: StateFlow<PendingCheckout?> = _current.asStateFlow()

    fun save(checkout: PendingCheckout) {
        if (_current.value == checkout) return
        payment = null
        _current.value = checkout
    }

    fun clear() {
        payment = null
        _current.value = null
    }

    fun savePayment(value: PendingPayment) { payment = value }
}

data class PendingPayment(
    val type: String, val itemId: String, val bookingId: String, val bookingReference: String,
    val orderId: String?, val amountPaise: Int?, val paymentUrl: String?, val paymentSessionId: String?
)
