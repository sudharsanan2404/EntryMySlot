package com.entrymyslot.app.data.model

/** Common contract used by discovery cards without copying movie, turf, or event data. */
interface CatalogItem {
    val id: String
    val title: String
    val date: String
    val location: String
    val price: String
    val imageUrl: String?
}

data class Movie(
    override val id: String,
    override val title: String,
    override val date: String,
    override val location: String,
    override val price: String,
    override val imageUrl: String? = null,
    val bannerUrl: String? = null,
    val description: String,
    val rating: Double,
    val language: String,
    val genre: String,
    val duration: String,
    val releaseDate: String,
    val castIds: List<String>,
    val castNames: List<String> = emptyList(),
    val director: String? = null,
    val trailerUrl: String? = null,
    val censorRating: String? = null,
    val ticketPrice: Int = 180,
) : CatalogItem

data class CastMember(
    val id: String,
    val name: String,
    val role: String,
    val imageUrl: String?
)

data class Cinema(
    val id: String,
    val name: String,
    val location: String
)

data class MovieShow(
    val id: String,
    val movieId: String,
    val cinemaId: String,
    val date: String,
    val time: String
)

data class MovieSeat(
    val showId: String,
    val label: String,
    val booked: Boolean
)
