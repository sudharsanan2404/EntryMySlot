package com.entrymyslot.app.data.mapper

import com.entrymyslot.app.data.model.Booking
import com.entrymyslot.app.data.model.BookingStatus
import com.entrymyslot.app.data.model.BookingType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.math.BigDecimal
import com.entrymyslotbe.app.network.model.Booking as MovieBooking
import com.entrymyslotbe.app.network.model.EventBooking
import com.entrymyslotbe.app.network.model.TurfBooking

internal fun bookingStatus(status: String?, date: String?, now: Instant = Instant.now()): BookingStatus? = when (status?.lowercase()) {
    "cancelled", "canceled", "failed", "expired" -> BookingStatus.CANCELLED
    "completed", "used", "checked_in", "attended" -> BookingStatus.COMPLETED
    "pending", "pending_payment", "payment_pending", "held" -> BookingStatus.PENDING
    "confirmed" -> if (bookingEndInstant(date)?.isBefore(now) == true) BookingStatus.COMPLETED else BookingStatus.UPCOMING
    else -> null // A pending or unknown reservation is not a confirmed ticket.
}
private fun bookingEndInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    val date = value.trim().replace(' ', 'T')
    return runCatching { Instant.parse(date) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(date).atZone(ZoneId.of("Asia/Kolkata")).toInstant() }.getOrNull()
        // A date-only reservation stays upcoming until that local calendar day ends.
        ?: runCatching { LocalDate.parse(date).plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant() }.getOrNull()
}

internal fun ticketDateTime(value: String?): Pair<String, String> {
    if (value.isNullOrBlank()) return "" to ""
    val raw = value.trim().replace(' ', 'T')
    val local = runCatching { Instant.parse(raw).atZone(ZoneId.of("Asia/Kolkata")).toLocalDateTime() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw) }.getOrNull()
    return if (local != null) local.toLocalDate().toString() to local.toLocalTime().toString().take(5)
    else raw.substringBefore('T') to raw.substringAfter('T', "").take(5)
}
internal fun BigDecimal?.paiseLabel(): String = this?.movePointLeft(2)?.toPlainString()?.let { "₹$it" }.orEmpty()
internal fun MovieBooking.toUiBooking(): Booking? {
    val b = booking ?: this
    val key = b.id ?: return null
    val date = b.showDatetime ?: showtime?.startTime
    return Booking(key, "", BookingType.MOVIE, b.movieId ?: movie?.id.orEmpty(), dateTime = date.orEmpty(),
        details = b.seatCount?.let { "$it seats" }.orEmpty(), price = b.totalAmount.paiseLabel(),
        status = bookingStatus(b.status, date) ?: return null, bookingReference = b.bookingReference ?: key,
        title = b.movieTitle ?: movie?.title.orEmpty(), location = b.cinemaName ?: showtime?.cinemaName.orEmpty())
}
internal fun EventBooking.toUiBooking(): Booking? {
    val key = id ?: return null
    return Booking(key, "", BookingType.EVENT, eventId.orEmpty(), dateTime = eventDate.orEmpty(),
        details = ticketCount?.let { "$it tickets" }.orEmpty(), price = totalAmount.paiseLabel(),
        status = bookingStatus(status, eventDate) ?: return null, bookingReference = bookingReference ?: key,
        title = eventTitle.orEmpty(), location = venue.orEmpty())
}
internal fun TurfBooking.toUiBooking(): Booking? {
    val key = id ?: return null
    return Booking(key, "", BookingType.TURF, resourceId.orEmpty(), venueId,
        dateTime = startsAt ?: date.orEmpty(), details = timeSlot.orEmpty(), price = totalAmount?.toPlainString()?.let { "₹$it" }.orEmpty(),
        status = bookingStatus(status, endsAt) ?: return null, bookingReference = bookingReference ?: key,
        title = resourceName ?: venueName.orEmpty(), location = venueName.orEmpty())
}
