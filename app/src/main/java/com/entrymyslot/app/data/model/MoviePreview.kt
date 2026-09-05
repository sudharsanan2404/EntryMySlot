package com.entrymyslot.app.data.details

data class MovieDetailDto(
    val id: Int, val title: String, val synopsis: String? = null, val genre: List<String> = emptyList(),
    val language: String = "", val durationMinutes: Int? = null, val cast: List<String> = emptyList(),
    val director: String? = null, val posterUrl: String? = null, val backdropUrl: String? = null,
    val trailerUrl: String? = null, val rating: Double? = null, val censorRating: String? = null,
    val releaseDate: String? = null, val status: String = ""
)
