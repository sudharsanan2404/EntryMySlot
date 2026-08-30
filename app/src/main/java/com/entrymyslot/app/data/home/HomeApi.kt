package com.entrymyslot.app.data.home

import com.entrymyslot.app.data.model.ApiResponse
import com.entrymyslot.app.data.model.EventDto
import com.entrymyslot.app.data.model.MovieDto
import com.entrymyslot.app.data.model.SportsVenueDto
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
