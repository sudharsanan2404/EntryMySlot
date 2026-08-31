package com.entrymyslot.app.data.search

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("types") types: String,
        @Query("price") price: String,
        @Query("sort") sort: String,
        @Query("city") city: String,
        @Query("seed") seed: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 24
    ): SearchResponse
}

@Serializable
data class SearchResponse(
    val success: Boolean,
    val data: SearchData
)

@Serializable
data class SearchData(
    val items: List<SearchItemDto> = emptyList(),
    val discoveryCards: List<SearchDiscoveryCardDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 24,
    val totalPages: Int = 0,
    val mode: String = "discovery"
)

@Serializable
data class SearchItemDto(
    val id: String,
    val type: String,
    val title: String,
    val date: String,
    val location: String,
    val price: String,
    val priceAmount: Double? = null,
    val imageUrl: String? = null
)

@Serializable
data class SearchDiscoveryCardDto(
    val id: String,
    val label: String,
    val title: String,
    val subtitle: String,
    val type: String
)
