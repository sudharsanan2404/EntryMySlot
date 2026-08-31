package com.entrymyslot.app.data.booking

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface BookingApi {
    @GET("bookings/my/all")
    suspend fun getMyBookings(
        @Query("tab") tab: String,
        @Query("type") type: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): MyBookingsResponse
}

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
