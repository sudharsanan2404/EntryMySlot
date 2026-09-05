package com.entrymyslot.app.data.booking

data class CinemaDto(val id: Int, val name: String, val address: String = "", val city: String = "", val state: String = "", val facilities: List<String> = emptyList(), val status: String = "active")
data class ShowtimeDto(val id: Int, val movieId: Int, val cinemaId: Int, val screenId: Int, val showDatetime: String, val endDatetime: String, val language: String, val format: String, val price: Int, val currency: String = "INR", val totalSeats: Int, val availableSeats: Int, val bookedSeats: Int = 0, val status: String)
data class MovieSeatLayoutDto(val showtimeId: Int, val screenId: Int, val price: Int, val currency: String, val rows: List<MovieSeatRowDto> = emptyList())
data class MovieSeatRowDto(val rowLabel: String, val seats: List<MovieSeatDto> = emptyList())
data class MovieSeatDto(val seatId: Int, val seatNumber: Int, val seatType: String, val seatCategory: String, val xPosition: Double? = null, val yPosition: Double? = null, val status: String, val pricePaise: Int) {
    val label get() = "$seatNumber"
    val tierKey get() = "$seatType:$seatCategory"
}
data class AuthoritativeBillDto(
    val domain: String, val quantity: Int, val subtotalPaise: Int, val discountPaise: Int = 0,
    val taxableAmountPaise: Int, val cgstPaise: Int, val sgstPaise: Int, val gstTotalPaise: Int,
    val platformFeePaise: Int, val totalPaise: Int, val currency: String, val calculatedAt: String
)
data class TurfSlotDto(
    val unit_id: Int, val starts_at: String, val ends_at: String, val status: String,
    val price: Double? = null, val currency: String = "INR", val formatted_time: String,
    val duration_minutes: Int, val blocked_reason: String? = null
)
