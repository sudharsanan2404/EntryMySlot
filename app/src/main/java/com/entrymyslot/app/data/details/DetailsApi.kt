package com.entrymyslot.app.data.details

import com.entrymyslot.app.data.model.Event
import com.entrymyslot.app.data.model.Movie
import com.entrymyslot.app.data.model.Turf
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.http.GET
import retrofit2.http.Path

interface DetailsApi {
    @GET("movies/{movieId}")
    suspend fun getMovieDetails(
        @Path("movieId") movieId: String
    ): MovieDetailResponse

    @GET("events/{eventId}")
    suspend fun getEventDetails(
        @Path("eventId") eventId: String
    ): EventDetailResponse

    @GET("turf/resources/{resourceId}")
    suspend fun getTurfDetails(
        @Path("resourceId") resourceId: String
    ): TurfDetailResponse
}

@Serializable
data class MovieDetailResponse(
    val success: Boolean,
    val data: MovieDetailDto
)

@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String,
    val synopsis: String? = null,
    val genre: List<String> = emptyList(),
    val language: String = "",
    val durationMinutes: Int? = null,
    val cast: List<String> = emptyList(),
    val director: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val trailerUrl: String? = null,
    val rating: Double? = null,
    val censorRating: String? = null,
    val releaseDate: String? = null,
    val status: String = ""
)

@Serializable
data class EventDetailResponse(
    val success: Boolean,
    val data: EventDetailDto
)

@Serializable
data class EventDetailDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val venue: String,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("start_at") val startAt: String? = null,
    val price: String = "0",
    val currency: String = "INR",
    @SerialName("is_free") val isFree: Boolean = false,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    val gallery: List<kotlinx.serialization.json.JsonElement> = emptyList(),
    val stats: EventStatsDto? = null
)

@Serializable
data class EventStatsDto(
    val capacity: Int = 0,
    val bookedCount: Int = 0,
    val remaining: Int = 0
)

@Serializable
data class TurfDetailResponse(
    val success: Boolean,
    val data: TurfDetailDto
)

@Serializable
data class TurfDetailDto(
    val id: Long,
    @SerialName("venue_id") val venueId: Long,
    @SerialName("resource_type") val resourceType: String,
    val category: String,
    val name: String,
    @SerialName("base_price") val basePrice: String,
    val attributes: JsonObject = JsonObject(emptyMap()),
    @SerialName("venue_name") val venueName: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val amenities: List<String> = emptyList(),
    val latitude: JsonPrimitive? = null,
    val longitude: JsonPrimitive? = null,
    @SerialName("avg_rating") val averageRating: JsonPrimitive? = null,
    @SerialName("review_count") val reviewCount: JsonPrimitive? = null
)

internal fun MovieDetailDto.toMovieModel(): Movie = Movie(
    id = id.toString(),
    title = title,
    date = status.toDisplayStatus(),
    location = listOf(language, genre.joinToString(", ")).filter(String::isNotBlank).joinToString(" · "),
    price = "View showtimes",
    imageUrl = posterUrl,
    bannerUrl = backdropUrl,
    description = synopsis.orEmpty(),
    rating = rating ?: 0.0,
    language = language,
    genre = genre.joinToString(", "),
    duration = durationMinutes.toDurationLabel(),
    releaseDate = releaseDate.toDisplayDate(),
    castIds = emptyList(),
    castNames = cast,
    director = director,
    trailerUrl = trailerUrl,
    censorRating = censorRating,
    ticketPrice = 0
)

internal fun EventDetailDto.toEventModel(): Event {
    val dateLabel = eventDate.toDisplayDate()
    val timeLabel = startTime.toDisplayTime()
    val locationLabel = listOfNotNull(
        venue.takeIf(String::isNotBlank),
        city?.takeIf { it.isNotBlank() && !venue.contains(it, ignoreCase = true) }
    ).joinToString(", ")
    val image = bannerUrl ?: thumbnailUrl ?: logoUrl ?: gallery.firstStringValue()
    return Event(
        id = id.toString(),
        title = title,
        date = listOf(dateLabel, timeLabel).filter(String::isNotBlank).joinToString(" | "),
        location = locationLabel,
        price = if (isFree || price.toDoubleOrNull() == 0.0) {
            "Free Entry"
        } else {
            "From ₹${price.toRupeeLabel()}"
        },
        imageUrl = image,
        description = description.orEmpty(),
        category = category.orEmpty(),
        time = timeLabel,
        remainingCapacity = stats?.remaining,
        endTime = endTime.toDisplayTime()
    )
}

internal fun TurfDetailDto.toTurfModel(): Turf {
    val imageUrls = attributes.stringList("imageUrls") + attributes.stringList("images")
    val sports = attributes.stringList("sports").ifEmpty { listOf(category) }
    return Turf(
        id = id.toString(),
        title = name,
        date = "Available Today",
        location = listOfNotNull(address, city).filter(String::isNotBlank).joinToString(", "),
        price = "From ₹${basePrice.toRupeeLabel()}",
        imageUrl = imageUrls.firstOrNull(),
        description = description.orEmpty(),
        rating = averageRating?.contentOrNull?.toDoubleOrNull() ?: 0.0,
        venueType = category.ifBlank { resourceType.replace('_', ' ').toTitleCase() },
        sports = sports,
        facilities = amenities,
        pricePerHour = basePrice.toDoubleOrNull()?.toInt() ?: 0,
        imageUrls = imageUrls.distinct(),
        specifications = attributes.pairList("specifications"),
        rules = attributes.stringList("rules"),
        venueId = venueId.toString(),
        resourceType = resourceType,
        reviewCount = reviewCount?.contentOrNull?.toIntOrNull() ?: 0
    )
}

private fun String.toDisplayStatus(): String =
    replace('_', ' ').toTitleCase()

private fun String.toTitleCase(): String =
    lowercase(Locale.ENGLISH).split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { it.titlecase(Locale.ENGLISH) }
    }

private fun Int?.toDurationLabel(): String {
    if (this == null || this <= 0) return ""
    val hours = this / 60
    val minutes = this % 60
    return buildList {
        if (hours > 0) add("${hours}h")
        if (minutes > 0) add("${minutes}m")
    }.joinToString(" ")
}

private fun String?.toDisplayDate(): String {
    if (isNullOrBlank()) return ""
    return runCatching {
        LocalDate.parse(take(10)).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
    }.getOrDefault(this)
}

private fun String?.toDisplayTime(): String {
    if (isNullOrBlank()) return ""
    return runCatching {
        LocalTime.parse(take(8)).format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
    }.getOrDefault(this)
}

private fun String.toRupeeLabel(): String {
    val amount = toDoubleOrNull() ?: return this
    return if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format(Locale.ENGLISH, "%.2f", amount)
}

private fun List<kotlinx.serialization.json.JsonElement>.firstStringValue(): String? =
    firstNotNullOfOrNull { element -> (element as? JsonPrimitive)?.contentOrNull }

private fun JsonObject.stringList(key: String): List<String> =
    (get(key) as? JsonArray)?.mapNotNull { element ->
        (element as? JsonPrimitive)?.contentOrNull
    }.orEmpty()

private fun JsonObject.pairList(key: String): List<Pair<String, String>> =
    (get(key) as? JsonObject)?.mapNotNull { (label, value) ->
        (value as? JsonPrimitive)?.contentOrNull?.let { label to it }
    }.orEmpty()
