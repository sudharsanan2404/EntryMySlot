package com.entrymyslot.app.data.home

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {
    @GET("home")
    suspend fun getHome(
        @Query("city") city: String
    ): HomeResponse
}

@Serializable
data class HomeResponse(
    val success: Boolean,
    val data: HomeData
)

@Serializable
data class HomeData(
    val selectedCity: String? = null,
    val promotions: List<HomePromotionDto> = emptyList(),
    val events: List<HomeCardDto> = emptyList(),
    val movies: List<HomeCardDto> = emptyList(),
    val sports: List<HomeCardDto> = emptyList()
)

@Serializable
data class HomePromotionDto(
    val id: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val cta: String,
    val destination: String,
    val imageUrl: String? = null
)

@Serializable
data class HomeCardDto(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val price: String,
    val imageUrl: String? = null
)
