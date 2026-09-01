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
    val zoneId: Int?,
    val zoneName: String,
    val attendees: List<PendingAttendee>,
    val subtotalPaise: Int,
    override val currency: String,
    override val bill: AuthoritativeBillDto
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
    val holdToken: String,
    val holdExpiresAt: String,
    val subtotalPaise: Int,
    override val currency: String,
    override val bill: AuthoritativeBillDto
) : PendingCheckout

class PendingCheckoutStore {
    private val _current = MutableStateFlow<PendingCheckout?>(null)
    val current: StateFlow<PendingCheckout?> = _current.asStateFlow()

    fun save(checkout: PendingCheckout) {
        _current.value = checkout
    }

    fun clear() {
        _current.value = null
    }
}
