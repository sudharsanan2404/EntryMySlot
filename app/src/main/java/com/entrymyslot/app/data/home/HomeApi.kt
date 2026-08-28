package com.entrymyslot.app.data.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {
    @GET("events/featured")
    suspend fun featuredEvents(@Query("limit") limit: Int = 10): Response<ApiResponse<List<EventDto>>>

    @GET("movies/movies/featured")
    suspend fun featuredMovies(@Query("limit") limit: Int = 10): Response<ApiResponse<List<MovieDto>>>

    @GET("turf/grounds")
    suspend fun nearbySports(@Query("pageSize") pageSize: Int = 10): Response<ApiResponse<List<SportsVenueDto>>>
}

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

@Serializable
data class EventDto(
    val id: Long,
    val title: String,
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("start_at") val startAt: String? = null,
    val venue: String? = null,
    val city: String? = null,
    val price: JsonElement? = null,
    @SerialName("is_free") val isFree: Boolean = false,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null
)

@Serializable
data class MovieDto(
    val id: Long,
    val title: String,
    val status: String? = null,
    val language: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null
)

@Serializable
data class SportsVenueDto(
    val id: Long,
    @SerialName("venue_name") val venueName: String? = null,
    val name: String? = null,
    val category: String? = null,
    val address: String? = null,
    val city: String? = null,
    @SerialName("base_price") val basePrice: JsonElement? = null
)