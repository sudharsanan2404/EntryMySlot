package com.entrymyslot.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────
// Movie list item   (GET /movies)
// ──────────────────────────────────────────────

@Serializable
data class MovieDto(
    val id: Long,
    val title: String,

    val slug: String? = null,
    val description: String? = null,
    val duration: Int? = null,
    val rating: String? = null,

    @SerialName("poster_url")
    val posterUrl: String? = null,

    @SerialName("banner_url")
    val bannerUrl: String? = null,

    @SerialName("release_date")
    val releaseDate: String? = null,

    val language: String? = null,
    val genres: List<String> = emptyList(),

    @SerialName("is_featured")
    val isFeatured: Boolean = false,

    @SerialName("average_rating")
    val averageRating: Double? = null,

    val certification: String? = null
)

// ──────────────────────────────────────────────
// Cinema   (GET /cinemas)
// ──────────────────────────────────────────────

@Serializable
data class CinemaDto(
    val id: Long,
    val name: String,
    val slug: String? = null,
    val city: String? = null,
    val address: String? = null,

    @SerialName("logo_url")
    val logoUrl: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("screens_count")
    val screensCount: Int? = null,

    val amenities: List<String> = emptyList()
)

// ──────────────────────────────────────────────
// Showtime   (GET /showtimes)
// ──────────────────────────────────────────────

@Serializable
data class ShowtimeDto(
    val id: Long,

    @SerialName("movie_id")
    val movieId: Long,

    @SerialName("cinema_id")
    val cinemaId: Long,

    @SerialName("screen_id")
    val screenId: Long? = null,

    @SerialName("show_date")
    val showDate: String,

    @SerialName("show_time")
    val showTime: String,

    val format: String? = null,
    val language: String? = null,

    @SerialName("available_seats")
    val availableSeats: Int = 0,

    @SerialName("total_seats")
    val totalSeats: Int = 0,

    @SerialName("base_price")
    val basePrice: String,

    val currency: String? = null,

    @SerialName("is_booking_open")
    val isBookingOpen: Boolean = true,

    @SerialName("movie_title")
    val movieTitle: String? = null,

    @SerialName("cinema_name")
    val cinemaName: String? = null
)

// ──────────────────────────────────────────────
// Seat layout
// ──────────────────────────────────────────────

@Serializable
data class SeatDto(
    val id: String,
    val row: String,
    val number: Int,
    val category: String,
    val status: String,

    @SerialName("price_multiplier")
    val priceMultiplier: Double = 1.0,

    @SerialName("display_label")
    val displayLabel: String? = null
)

@Serializable
data class ShowtimeSeatsData(
    @SerialName("showtime_id")
    val showtimeId: Long,

    @SerialName("movie_title")
    val movieTitle: String? = null,

    @SerialName("cinema_name")
    val cinemaName: String? = null,

    val screen: String? = null,

    @SerialName("show_date")
    val showDate: String? = null,

    @SerialName("show_time")
    val showTime: String? = null,

    val seats: List<SeatDto> = emptyList()
)

@Serializable
data class ShowtimeSeatsWrapper(
    val success: Boolean,
    val data: ShowtimeSeatsData? = null
)

// ──────────────────────────────────────────────
// Price calculation
// ──────────────────────────────────────────────

@Serializable
data class SeatPriceItem(
    @SerialName("seat_id")
    val seatId: String,

    val category: String,

    @SerialName("base_price")
    val basePrice: String,

    @SerialName("final_price")
    val finalPrice: String,

    @SerialName("price_multiplier")
    val priceMultiplier: Double = 1.0
)

@Serializable
data class PriceBreakdownDto(
    val subtotal: String,
    val taxes: String,
    val fees: String,
    val total: String,
    val currency: String? = null
)

@Serializable
data class CalculatePricesRequest(
    @SerialName("showtime_id")
    val showtimeId: Long,

    @SerialName("seat_ids")
    val seatIds: List<String>
)

@Serializable
data class CalculatePricesData(
    @SerialName("showtime_id")
    val showtimeId: Long,

    @SerialName("seat_prices")
    val seatPrices: List<SeatPriceItem>,

    val breakdown: PriceBreakdownDto
)

@Serializable
data class CalculatePricesWrapper(
    val success: Boolean,
    val data: CalculatePricesData? = null
)