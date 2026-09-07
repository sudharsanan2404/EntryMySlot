package com.entrymyslotbe.app.network.model
import com.google.gson.annotations.SerializedName

// ========== MOVIE MODELS ==========
data class MovieSearchPage(val items: List<Movie> = emptyList(), val total: Int = 0, val page: Int = 1, val totalPages: Int = 0)

data class Movie(
    val id: String? = null,
    val slug: String? = null,
    val title: String? = null,
    @SerializedName(value = "description", alternate = ["synopsis"])
    val description: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    @SerializedName(value = "duration", alternate = ["durationMinutes"])
    val duration: Int? = null,
    val language: String? = null,
    val genre: List<String>? = null,
    val rating: Double? = null,
    val status: String? = null,
    val isFeatured: Boolean = false,
    val releaseDate: String? = null,
    val cast: List<String>? = null,
    val director: String? = null,
    val trailerUrl: String? = null,
    val censorRating: String? = null
)

data class MovieFilters(
    val city: String? = null,
    val genre: String? = null,
    val language: String? = null,
    val status: String? = null,
    val featured: Boolean? = null,
    val search: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20
)

data class Cinema(
    val id: String? = null,
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val facilities: List<String>? = null,
    val screens: List<Screen>? = null
)

data class Screen(
    val id: String? = null,
    val name: String? = null,
    val cinemaId: String? = null,
    val seatCapacity: Int? = null,
    val screenType: String? = null
)

data class Showtime(
    val id: String? = null,
    val movieId: String? = null,
    val movieTitle: String? = null,
    val cinemaId: String? = null,
    val cinemaName: String? = null,
    val screenId: String? = null,
    val screenName: String? = null,
    @SerializedName(value = "startTime", alternate = ["showDatetime"])
    val startTime: String? = null,
    @SerializedName(value = "endTime", alternate = ["endDatetime"])
    val endTime: String? = null,
    val language: String? = null,
    val format: String? = null,
    val date: String? = null,
    val price: Int? = null,
    val currency: String = "INR",
    val status: String? = null,
    val availableSeats: Int? = null,
    val totalSeats: Int? = null
)

data class ShowtimeFilters(
    val movieId: String? = null,
    val city: String? = null,
    val date: String? = null,
    val cinemaId: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20
)

data class Seat(
    @SerializedName(value = "id", alternate = ["seatId", "seat_id"])
    val id: String,
    @SerializedName(value = "row", alternate = ["rowLabel", "row_label"])
    val row: String? = null,
    @SerializedName(value = "number", alternate = ["seatNumber", "seat_number"])
    val number: String,
    @SerializedName(value = "type", alternate = ["seatType", "seat_type"])
    val type: String,      // standard, premium, sofa, couple, wheelchair
    val seatCategory: String? = null,
    val xPosition: Double? = null,
    val yPosition: Double? = null,
    val status: String,     // available, held, booked
    @SerializedName(value = "price", alternate = ["pricePaise"])
    val price: Int? = null
)

data class SeatRow(val rowLabel: String, val seats: List<Seat> = emptyList())

data class SeatsResponse(
    val showtimeId: String? = null,
    val screenId: String? = null,
    val price: Int? = null,
    val currency: String = "INR",
    val rows: List<SeatRow>? = null
)

data class PriceBreakdown(
    val seatId: String? = null,
    @SerializedName(value = "basePrice", alternate = ["basePricePaise"])
    val basePrice: Int? = null,
    val multiplier: Double? = null,
    @SerializedName(value = "finalPrice", alternate = ["finalPricePaise"])
    val finalPrice: Int? = null
)

data class CalculatePricesRequest(
    val seatIds: List<String>
)

// Booking
data class MovieBookingRequest(
    val holdKey: String,
    val customerEmail: String,
    val customerPhone: String,
    val customerName: String,
    val notes: String? = null,
    val idempotencyKey: String? = null
)

data class MovieBookingResponse(
    val booking: Booking? = null,
    val paymentOrderId: String? = null,
    val paymentSessionId: String? = null,
    val paymentUrl: String? = null
)

data class Booking(
    val id: String? = null,
    val booking: Booking? = null,
    @SerializedName(value = "movieId", alternate = ["movie_id"]) val movieId: String? = null,
    @SerializedName(value = "movieTitle", alternate = ["movie_title"]) val movieTitle: String? = null,
    @SerializedName(value = "cinemaName", alternate = ["cinema_name"]) val cinemaName: String? = null,
    @SerializedName(value = "showDatetime", alternate = ["show_datetime"]) val showDatetime: String? = null,
    @SerializedName(value = "seatCount", alternate = ["seat_count"]) val seatCount: Int? = null,
    @SerializedName(value = "bookingReference", alternate = ["booking_reference"])
    val bookingReference: String? = null,
    val status: String? = null,  // pending_payment, confirmed, failed, cancelled
    @SerializedName(value = "customerEmail", alternate = ["customer_email"])
    val customerEmail: String? = null,
    @SerializedName(value = "customerPhone", alternate = ["customer_phone"])
    val customerPhone: String? = null,
    @SerializedName(value = "customerName", alternate = ["customer_name"])
    val customerName: String? = null,
    val seats: List<Seat>? = null,
    val showtime: Showtime? = null,
    val movie: Movie? = null,
    @SerializedName(value = "totalAmount", alternate = ["amount", "total_amount"])
    val totalAmount: java.math.BigDecimal? = null,
    val currency: String? = null,
    val createdAt: String? = null,
    val confirmedAt: String? = null,
    val tickets: List<Ticket>? = null
)

data class HoldSeatsRequest(
    val seatIds: List<String>,
    val showtimeId: String
)

data class HoldSeatsResponse(
    val success: Boolean = false,
    val active: Boolean? = null,
    val ttlSeconds: Int? = null,
    @SerializedName(value = "heldSeatIds", alternate = ["seatIds"])
    val heldSeatIds: List<String>? = null,
    val conflictedSeatIds: List<String>? = null,
    @SerializedName(value = "holdExpiresAt", alternate = ["expiresAt"])
    val holdExpiresAt: String? = null,
    val holdKey: String? = null
)

data class Ticket(
    val id: String? = null,
    @SerializedName(value = "ticketUuid", alternate = ["ticket_uuid"])
    val ticketUuid: String? = null,
    @SerializedName(value = "qrData", alternate = ["qr_data"]) val qrData: String? = null,
    @SerializedName(value = "attendeeName", alternate = ["attendee_name"]) val attendeeName: String? = null,
    val movieTitle: String? = null,
    val cinemaName: String? = null,
    val showtimeDatetime: String? = null,
    val seatLabel: String? = null,
    val signature: String? = null,
    val status: String? = null,  // valid, used, expired, invalid
    val seat: Seat? = null,
    val qrCodeBase64: String? = null,
    val bookingReference: String? = null
)

data class TicketVerificationResponse(
    val valid: Boolean,
    val ticket: Ticket? = null,
    val signatureValid: Boolean? = null
)
