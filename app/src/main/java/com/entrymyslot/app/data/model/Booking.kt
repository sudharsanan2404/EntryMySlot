package com.entrymyslot.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────
// Wrapper used across all booking endpoints
// ──────────────────────────────────────────────

@Serializable
data class ApiWrapper(
    val success: Boolean,
    val message: String? = null,
    val data: kotlinx.serialization.json.JsonElement? = null
)

// ──────────────────────────────────────────────
// Common booking status
// ──────────────────────────────────────────────

@Serializable
data class BookingStatusResponse(
    val status: String
)

// ──────────────────────────────────────────────
// Event Booking
// ──────────────────────────────────────────────

@Serializable
data class CreateEventBookingRequest(
    @SerialName("event_id")
    val eventId: Long,

    @SerialName("ticket_quantity")
    val ticketQuantity: Int,

    @SerialName("attendee_name")
    val attendeeName: String,

    @SerialName("attendee_phone")
    val attendeePhone: String,

    @SerialName("coupon_code")
    val couponCode: String? = null
)

@Serializable
data class AttendeeTicketDto(
    @SerialName("ticket_uuid")
    val ticketUuid: String,

    @SerialName("attendee_name")
    val attendeeName: String,

    @SerialName("attendee_phone")
    val attendeePhone: String,

    val signature: String? = null
)

@Serializable
data class PaymentInfoDto(
    @SerialName("order_id")
    val orderId: String,

    val amount: String,
    val currency: String? = null,

    @SerialName("payment_session_id")
    val paymentSessionId: String? = null
)

@Serializable
data class CreateEventBookingResponse(
    @SerialName("booking_id")
    val bookingId: String,

    val status: String,

    @SerialName("ticket_count")
    val ticketCount: Int,

    val tickets: List<AttendeeTicketDto> = emptyList(),

    val payment: PaymentInfoDto? = null
)

// ──────────────────────────────────────────────
// Movie Booking
// ──────────────────────────────────────────────

@Serializable
data class CreateMovieBookingRequest(
    @SerialName("showtime_id")
    val showtimeId: Long,

    @SerialName("seat_ids")
    val seatIds: List<String>,

    @SerialName("booking_type")
    val bookingType: String = "online"
)

@Serializable
data class CreateMovieBookingResponse(
    @SerialName("booking_reference")
    val bookingReference: String,

    val status: String,

    @SerialName("showtime_id")
    val showtimeId: Long,

    @SerialName("seat_ids")
    val seatIds: List<String>
)

@Serializable
data class HoldSeatsRequest(
    @SerialName("showtime_id")
    val showtimeId: Long,

    @SerialName("seat_ids")
    val seatIds: List<String>,

    @SerialName("hold_minutes")
    val holdMinutes: Int = 5
)

@Serializable
data class HoldSeatsResponse(
    @SerialName("hold_id")
    val holdId: String,

    val status: String,

    @SerialName("expires_at")
    val expiresAt: String
)

@Serializable
data class VerifyTicketRequest(
    @SerialName("ticket_uuid")
    val ticketUuid: String
)

@Serializable
data class VerifyTicketResponse(
    val valid: Boolean,
    val message: String? = null,

    @SerialName("booking_reference")
    val bookingReference: String? = null
)

// ──────────────────────────────────────────────
// Turf Booking
// ──────────────────────────────────────────────

@Serializable
data class CreateTurfBookingRequest(
    @SerialName("availability_unit_id")
    val availabilityUnitId: Long,

    val quantity: Int,

    @SerialName("booking_type")
    val bookingType: String,

    @SerialName("coupon_code")
    val couponCode: String? = null,

    val amount: String,

    @SerialName("duration_hours")
    val durationHours: Int
)

@Serializable
data class CreateTurfBookingResponse(
    @SerialName("booking_id")
    val bookingId: String,

    val status: String,

    @SerialName("resource_id")
    val resourceId: Long,

    val amount: String,
    val currency: String? = null
)

// ──────────────────────────────────────────────
// Shared booking list items
// ──────────────────────────────────────────────

@Serializable
data class EventBookingListItem(
    @SerialName("booking_id")
    val bookingId: String,

    @SerialName("event_id")
    val eventId: Long,

    @SerialName("event_title")
    val eventTitle: String,

    @SerialName("event_date")
    val eventDate: String? = null,

    val status: String,

    @SerialName("ticket_count")
    val ticketCount: Int,

    @SerialName("total_amount")
    val totalAmount: String,

    val currency: String? = null,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class MovieBookingListItem(
    @SerialName("booking_reference")
    val bookingReference: String,

    @SerialName("showtime_id")
    val showtimeId: Long,

    @SerialName("movie_title")
    val movieTitle: String,

    @SerialName("cinema_name")
    val cinemaName: String? = null,

    @SerialName("show_date")
    val showDate: String? = null,

    @SerialName("show_time")
    val showTime: String? = null,

    @SerialName("seat_count")
    val seatCount: Int,

    val status: String,

    @SerialName("total_amount")
    val totalAmount: String,

    val currency: String? = null,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class TurfBookingListItem(
    @SerialName("booking_id")
    val bookingId: String,

    @SerialName("venue_id")
    val venueId: Long,

    @SerialName("venue_name")
    val venueName: String,

    @SerialName("resource_name")
    val resourceName: String? = null,

    @SerialName("booking_date")
    val bookingDate: String? = null,

    @SerialName("duration_hours")
    val durationHours: Int,

    val status: String,

    val amount: String,
    val currency: String? = null,

    @SerialName("created_at")
    val createdAt: String
)

// ──────────────────────────────────────────────
// Unified booking list response
// ──────────────────────────────────────────────

@Serializable
data class MyBookingsResponse(
    val events: List<EventBookingListItem> = emptyList(),
    val movies: List<MovieBookingListItem> = emptyList(),
    val turfs: List<TurfBookingListItem> = emptyList()
)

// ──────────────────────────────────────────────
// Booking detail responses
// ──────────────────────────────────────────────

@Serializable
data class EventBookingDetailResponse(
    @SerialName("booking_id")
    val bookingId: String,

    val status: String,

    val event: EventDetailResponse? = null,

    @SerialName("ticket_count")
    val ticketCount: Int,

    val tickets: List<AttendeeTicketDto> = emptyList(),

    @SerialName("total_amount")
    val totalAmount: String,

    val currency: String? = null,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class MovieBookingDetailResponse(
    @SerialName("booking_reference")
    val bookingReference: String,

    val status: String,

    val movie: MovieDto? = null,
    val cinema: CinemaDto? = null,
    val showtime: ShowtimeDto? = null,

    val seats: List<SeatDto> = emptyList(),

    @SerialName("total_amount")
    val totalAmount: String,

    val currency: String? = null,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class TurfBookingDetailResponse(
    @SerialName("booking_id")
    val bookingId: String,

    val status: String,

    val venue: SportsVenueDto? = null,
    val resource: ResourceDto? = null,

    @SerialName("quantity")
    val quantity: Int,

    @SerialName("duration_hours")
    val durationHours: Int,

    val amount: String,
    val currency: String? = null,

    @SerialName("created_at")
    val createdAt: String
)