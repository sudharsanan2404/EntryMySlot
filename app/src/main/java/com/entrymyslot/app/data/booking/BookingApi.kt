package com.entrymyslot.app.data.booking

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingApi {
    @GET("bookings/my/all")
    suspend fun getMyBookings(
        @Query("tab") tab: String,
        @Query("type") type: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): MyBookingsResponse

    @GET("movies/cinemas")
    suspend fun getCinemas(): CinemaListResponse

    @GET("movies/showtimes")
    suspend fun getMovieShowtimes(
        @Query("movieId") movieId: Int,
        @Query("date") date: String? = null
    ): ShowtimeListResponse

    @GET("movies/showtimes/{showtimeId}/seats")
    suspend fun getMovieSeatLayout(
        @Path("showtimeId") showtimeId: Int
    ): MovieSeatLayoutResponse

    @POST("movies/hold-seats")
    suspend fun holdMovieSeats(
        @Body request: MovieSeatHoldRequest
    ): MovieSeatHoldResponse

    @GET("movies/hold-seats/{holdKey}/status")
    suspend fun getMovieHoldStatus(
        @Path("holdKey") holdKey: String
    ): MovieHoldStatusResponse

    @GET("movies/hold-seats/current")
    suspend fun getCurrentMovieHold(@Query("showtimeId") showtimeId: Int): MovieHoldStatusResponse

    @POST("movies/hold-seats/{holdKey}/release")
    suspend fun releaseMovieSeats(
        @Path("holdKey") holdKey: String
    ): BasicSuccessResponse

    @POST("movies/showtimes/{showtimeId}/calculate-prices")
    suspend fun calculateMoviePrices(
        @Path("showtimeId") showtimeId: Int,
        @Body request: MoviePriceRequest
    ): MoviePriceResponse

    @GET("events/{eventId}/zones")
    suspend fun getEventZones(
        @Path("eventId") eventId: String
    ): EventZonesResponse

    @POST("bookings/holds/events")
    suspend fun holdEvent(@Body request: EventHoldRequest): EventHoldResponse

    @GET("bookings/holds/events/current")
    suspend fun getCurrentEventHold(@Query("eventId") eventId: Int): EventHoldResponse

    @POST("bookings/holds/events/{holdKey}/release")
    suspend fun releaseEventHold(@Path("holdKey") holdKey: String): BasicSuccessResponse

    @GET("turf/resources/{resourceId}/availability")
    suspend fun getTurfAvailability(
        @Path("resourceId") resourceId: String,
        @Query("date") date: String
    ): TurfAvailabilityResponse

    @POST("turf/holds")
    suspend fun holdTurfSlot(
        @Body request: TurfHoldRequest
    ): TurfHoldResponse

    @GET("turf/holds/{token}/status")
    suspend fun getTurfHoldStatus(
        @Path("token") token: String
    ): TurfHoldStatusResponse

    @GET("turf/holds/current")
    suspend fun getCurrentTurfHold(@Query("resourceId") resourceId: Int): TurfHoldStatusResponse

    @POST("turf/holds/{token}/release")
    suspend fun releaseTurfHold(
        @Path("token") token: String
    ): TurfHoldReleaseResponse

    @POST("movies/bookings")
    suspend fun createMovieBooking(@Body request: MovieBookingCreateRequest): MovieBookingCreateResponse

    @POST("movies/bookings/confirm")
    suspend fun confirmMovieBooking(@Body request: MovieBookingConfirmRequest): MovieBookingDetailsResponse

    @GET("movies/bookings/{reference}")
    suspend fun getMovieBooking(@Path("reference") reference: String): MovieBookingDetailsResponse

    @GET("movies/bookings/{reference}/tickets")
    suspend fun getMovieTickets(@Path("reference") reference: String): MovieTicketsResponse

    @POST("bookings")
    suspend fun createEventBooking(@Body request: EventBookingCreateRequest): EventBookingCreateResponse

    @POST("bookings/{bookingId}/verify")
    suspend fun verifyEventBooking(@Path("bookingId") bookingId: Int): EventBookingVerifyResponse

    @GET("bookings/{bookingId}")
    suspend fun getEventBooking(@Path("bookingId") bookingId: Int): EventBookingDetailsResponse

    @POST("turf/bookings")
    suspend fun createTurfBooking(@Body request: TurfBookingCreateRequest): TurfBookingCreateResponse

    @POST("turf/payments/create-order")
    suspend fun createTurfPaymentOrder(@Body request: TurfPaymentCreateRequest): TurfPaymentCreateResponse

    @POST("turf/payments/verify")
    suspend fun verifyTurfPayment(@Body request: TurfPaymentVerifyRequest): TurfPaymentVerifyResponse

    @GET("turf/my/bookings/{bookingId}")
    suspend fun getTurfBooking(@Path("bookingId") bookingId: Int): TurfBookingDetailsResponse
}

@Serializable
data class MovieBookingCreateRequest(
    val holdKey: String,
    val idempotencyKey: String,
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerName: String = ""
)

@Serializable
data class MovieBookingConfirmRequest(
    val bookingReference: String,
    val paymentOrderId: String? = null
)

@Serializable
data class MovieBookingCreateResponse(val success: Boolean, val data: MovieBookingCreateDto)

@Serializable
data class MovieBookingCreateDto(
    val booking: MovieBookingServerDto,
    val paymentOrderId: String,
    val paymentSessionId: String = ""
)

@Serializable
data class MovieBookingDetailsResponse(val success: Boolean, val data: MovieBookingDetailsDto)

@Serializable
data class MovieBookingDetailsDto(
    val booking: MovieBookingServerDto,
    val movie: MovieBookingMovieDto,
    val cinema: MovieBookingCinemaDto,
    val showtime: MovieBookingShowtimeDto,
    val items: List<MovieBookingItemDto> = emptyList()
)

@Serializable
data class MovieBookingServerDto(
    val id: Int,
    @SerialName("booking_reference") val bookingReference: String,
    val amount: JsonPrimitive = JsonPrimitive(0),
    val currency: String = "INR",
    val status: String = "",
    @SerialName("seat_count") val seatCount: Int = 0
)

@Serializable
data class MovieBookingMovieDto(
    val id: Int,
    @SerialName("movie_title") val title: String
)

@Serializable
data class MovieBookingCinemaDto(
    val id: Int,
    @SerialName("cinema_name") val name: String,
    @SerialName("cinema_address") val address: String = "",
    @SerialName("cinema_city") val city: String = ""
)

@Serializable
data class MovieBookingShowtimeDto(
    val id: Int,
    @SerialName("show_datetime") val showDatetime: String,
    @SerialName("end_datetime") val endDatetime: String = ""
)

@Serializable
data class MovieBookingItemDto(
    @SerialName("seat_label") val seatLabel: String,
    val ticket: MovieTicketDto? = null
)

@Serializable
data class MovieTicketsResponse(val success: Boolean, val data: List<MovieTicketDto> = emptyList())

@Serializable
data class MovieTicketDto(
    val ticketUuid: String,
    val seatLabel: String,
    val status: String = "valid"
)

@Serializable
data class EventBookingCreateRequest(
    @SerialName("event_id") val eventId: Int,
    val attendees: List<EventBookingAttendeeRequest>,
    @SerialName("zone_id") val zoneId: Int? = null,
    val holdKey: String
)

@Serializable
data class EventBookingAttendeeRequest(
    @SerialName("full_name") val fullName: String,
    val phone: String
)

@Serializable
data class EventBookingCreateResponse(val success: Boolean, val data: EventBookingCreateDto)

@Serializable
data class EventBookingCreateDto(
    val bookingId: Int,
    val status: String,
    val ticketCount: Int = 0,
    val tickets: List<EventTicketSummaryDto> = emptyList(),
    val payment: EventPaymentDto? = null
)

@Serializable
data class EventTicketSummaryDto(
    val ticketUuid: String,
    val attendeeName: String = "",
    val attendeePhone: String = ""
)

@Serializable
data class EventPaymentDto(val orderId: String, val amount: Int = 0, val currency: String = "INR")

@Serializable
data class EventBookingVerifyResponse(val success: Boolean, val data: EventBookingVerifyDto)

@Serializable
data class EventBookingVerifyDto(val bookingId: Int, val status: String, val message: String = "")

@Serializable
data class EventBookingDetailsResponse(val success: Boolean, val data: EventBookingDetailsDto)

@Serializable
data class EventBookingDetailsDto(
    val booking: EventBookingServerDto,
    val tickets: List<EventTicketDto> = emptyList()
)

@Serializable
data class EventBookingServerDto(
    val id: Int,
    @SerialName("event_id") val eventId: Int,
    @SerialName("ticket_count") val ticketCount: Int,
    val status: String
)

@Serializable
data class EventTicketDto(
    @SerialName("ticket_uuid") val ticketUuid: String,
    @SerialName("attendee_name") val attendeeName: String,
    @SerialName("attendee_phone") val attendeePhone: String = ""
)

@Serializable
data class TurfBookingCreateRequest(
    @SerialName("availability_unit_id") val availabilityUnitId: Int,
    @SerialName("hold_token") val holdToken: String,
    val quantity: Int = 1,
    @SerialName("booking_type") val bookingType: String = "online"
)

@Serializable
data class TurfBookingCreateResponse(val success: Boolean, val data: TurfBookingCreateDto)

@Serializable
data class TurfBookingCreateDto(val booking: TurfBookingServerDto)

@Serializable
data class TurfPaymentCreateRequest(val bookingId: Int)

@Serializable
data class TurfPaymentCreateResponse(val success: Boolean, val data: TurfPaymentCreateDto)

@Serializable
data class TurfPaymentCreateDto(val order: PaymentOrderDto, val paymentSessionId: String = "")

@Serializable
data class PaymentOrderDto(@SerialName("order_id") val orderId: String)

@Serializable
data class TurfPaymentVerifyRequest(val bookingId: Int, val gatewayOrderId: String)

@Serializable
data class TurfPaymentVerifyResponse(val success: Boolean, val data: TurfPaymentVerifyDto)

@Serializable
data class TurfPaymentVerifyDto(val status: String, val booking: TurfBookingServerDto)

@Serializable
data class TurfBookingDetailsResponse(val success: Boolean, val data: TurfBookingServerDto)

@Serializable
data class TurfBookingServerDto(
    val id: Int,
    @SerialName("booking_reference") val bookingReference: String,
    val amount: JsonPrimitive = JsonPrimitive(0),
    val currency: String = "INR",
    val status: String,
    @SerialName("venue_name") val venueName: String = "",
    @SerialName("resource_name") val resourceName: String = "",
    @SerialName("slot_start") val slotStart: String = "",
    @SerialName("slot_end") val slotEnd: String = "",
    @SerialName("qr_token") val qrToken: String? = null,
    @SerialName("customer_name") val customerName: String? = null
)

@Serializable
data class BasicSuccessResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class CinemaListResponse(
    val success: Boolean,
    val data: List<CinemaDto> = emptyList()
)

@Serializable
data class CinemaDto(
    val id: Int,
    val name: String,
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val facilities: List<String> = emptyList(),
    val status: String = "active"
)

@Serializable
data class ShowtimeListResponse(
    val success: Boolean,
    val data: List<ShowtimeDto> = emptyList()
)

@Serializable
data class ShowtimeDto(
    val id: Int,
    val movieId: Int,
    val cinemaId: Int,
    val screenId: Int,
    val showDatetime: String,
    val endDatetime: String,
    val language: String,
    val format: String,
    val price: Int,
    val currency: String = "INR",
    val totalSeats: Int,
    val availableSeats: Int,
    val bookedSeats: Int = 0,
    val status: String
)

@Serializable
data class MovieSeatLayoutResponse(
    val success: Boolean,
    val data: MovieSeatLayoutDto
)

@Serializable
data class MovieSeatLayoutDto(
    val showtimeId: Int,
    val screenId: Int,
    val price: Int,
    val currency: String,
    val rows: List<MovieSeatRowDto> = emptyList()
)

@Serializable
data class MovieSeatRowDto(
    val rowLabel: String,
    val seats: List<MovieSeatDto> = emptyList()
)

@Serializable
data class MovieSeatDto(
    val seatId: Int,
    val seatNumber: Int,
    val seatType: String,
    val seatCategory: String,
    val xPosition: Double? = null,
    val yPosition: Double? = null,
    val status: String,
    val pricePaise: Int
) {
    val label: String get() = "$seatNumber"
    val tierKey: String get() = "$seatType:$seatCategory"
}

@Serializable
data class MovieSeatHoldRequest(
    val showtimeId: Int,
    val seatIds: List<Int>,
    val holdKey: String? = null
)

@Serializable
data class MovieSeatHoldResponse(
    val success: Boolean,
    val data: MovieSeatHoldDto,
    val message: String? = null
)

@Serializable
data class MovieSeatHoldDto(
    val success: Boolean,
    val heldSeatIds: List<Int> = emptyList(),
    val conflictedSeatIds: List<Int> = emptyList(),
    val holdExpiresAt: String = "",
    val holdKey: String = "",
    val ttlSeconds: Int = 0,
    val bill: AuthoritativeBillDto? = null
)

@Serializable
data class MovieHoldStatusResponse(
    val success: Boolean,
    val data: MovieHoldStatusDto
)

@Serializable
data class MovieHoldStatusDto(
    val active: Boolean,
    val ttlSeconds: Int = 0,
    val seatIds: List<Int> = emptyList(),
    val expiresAt: String? = null,
    val showtimeId: Int? = null,
    val bill: AuthoritativeBillDto? = null,
    val holdKey: String? = null
)

@Serializable
data class AuthoritativeBillDto(
    val domain: String,
    val quantity: Int,
    val subtotalPaise: Int,
    val discountPaise: Int = 0,
    val taxableAmountPaise: Int,
    val cgstPaise: Int,
    val sgstPaise: Int,
    val gstTotalPaise: Int,
    val platformFeePaise: Int,
    val totalPaise: Int,
    val currency: String,
    val calculatedAt: String
)

@Serializable
data class MoviePriceRequest(val seatIds: List<Int>)

@Serializable
data class MoviePriceResponse(
    val success: Boolean,
    val data: List<MovieSeatPriceDto> = emptyList()
)

@Serializable
data class MovieSeatPriceDto(
    val seatId: Int,
    val basePricePaise: Int,
    val finalPricePaise: Int,
    val seatType: String,
    val capped: Boolean = false,
    val capReason: String? = null
)

@Serializable
data class EventZonesResponse(
    val zones: List<EventZoneDto> = emptyList()
)

@Serializable
data class EventZoneDto(
    val id: Int,
    val event_id: Int,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val total_capacity: Int,
    val remaining_capacity: Int,
    val price: Double,
    val currency: String = "INR",
    val sort_order: Int = 0,
    val is_active: Boolean = true
)

@Serializable
data class EventHoldRequest(val eventId: Int, val quantity: Int, val zoneId: Int? = null)

@Serializable
data class EventHoldResponse(val success: Boolean, val data: EventHoldDto)

@Serializable
data class EventHoldDto(
    val active: Boolean,
    val holdKey: String? = null,
    val eventId: Int? = null,
    val zoneId: Int? = null,
    val quantity: Int = 0,
    val expiresAt: String? = null,
    val ttlSeconds: Int = 0,
    val bill: AuthoritativeBillDto? = null
)

@Serializable
data class TurfAvailabilityResponse(
    val success: Boolean,
    val data: TurfAvailabilityDto
)

@Serializable
data class TurfAvailabilityDto(
    val resource_id: Int,
    val resource_name: String = "",
    val venue_id: Int,
    val venue_name: String = "",
    val date: String,
    val timezone: String,
    val slots: List<TurfSlotDto> = emptyList(),
    val summary: TurfAvailabilitySummaryDto = TurfAvailabilitySummaryDto()
)

@Serializable
data class TurfSlotDto(
    val unit_id: Int,
    val starts_at: String,
    val ends_at: String,
    val status: String,
    val price: Double? = null,
    val currency: String = "INR",
    val formatted_time: String,
    val duration_minutes: Int,
    val blocked_reason: String? = null
)

@Serializable
data class TurfAvailabilitySummaryDto(
    val available: Int = 0,
    val held: Int = 0,
    val booked: Int = 0,
    val blocked: Int = 0,
    val unavailable: Int = 0
)

@Serializable
data class TurfHoldRequest(val unitId: Int)

@Serializable
data class TurfHoldResponse(
    val success: Boolean,
    val data: TurfHoldDto
)

@Serializable
data class TurfHoldDto(
    val success: Boolean,
    val token: String,
    val unitId: Int,
    val expiresAt: String,
    val bill: AuthoritativeBillDto
)

@Serializable
data class TurfHoldStatusResponse(
    val success: Boolean,
    val data: TurfHoldStatusDto
)

@Serializable
data class TurfHoldStatusDto(
    val active: Boolean,
    val unitId: Int? = null,
    val expiresAt: String? = null,
    val ttlSeconds: Int = 0,
    val status: String,
    val token: String? = null,
    val bill: AuthoritativeBillDto? = null
)

@Serializable
data class TurfHoldReleaseResponse(
    val success: Boolean,
    val data: TurfHoldReleaseDto
)

@Serializable
data class TurfHoldReleaseDto(
    val success: Boolean,
    val reason: String
)

@Serializable
data class MyBookingsResponse(
    val success: Boolean,
    val data: List<BookingDto> = emptyList(),
    val pagination: BookingPaginationDto = BookingPaginationDto()
)

@Serializable
data class BookingPaginationDto(
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 50,
    val totalPages: Int = 0
)

@Serializable
data class BookingDto(
    val id: String,
    val bookingId: String,
    val bookingReference: String,
    val userId: String,
    val type: String,
    val itemId: String,
    val venueId: String? = null,
    val title: String,
    val location: String,
    val dateTime: String,
    val details: String,
    val price: String,
    val status: String
)
