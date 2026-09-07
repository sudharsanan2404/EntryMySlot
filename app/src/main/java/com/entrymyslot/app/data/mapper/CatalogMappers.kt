package com.entrymyslot.app.data.mapper

import com.entrymyslot.app.data.model.*
import com.entrymyslot.app.data.details.MovieDetailDto
import com.entrymyslotbe.app.network.model.Movie as ApiMovie
import com.entrymyslotbe.app.network.model.Event as ApiEvent
import com.entrymyslotbe.app.network.model.Venue
import com.entrymyslotbe.app.network.model.User

internal fun ApiMovie.toUi() = id?.takeIf(String::isNotBlank)?.let { key ->
    Movie(key, title.orEmpty(), releaseDate.orEmpty().substringBefore('T'), "", "", posterUrl, backdropUrl,
        description.orEmpty(), rating, language.orEmpty(), genre.orEmpty().joinToString(", "),
        duration?.let { "$it min" }.orEmpty(), releaseDate.orEmpty(), emptyList(), cast.orEmpty(), director,
        trailerUrl = trailerUrl, censorRating = censorRating, ticketPrice = null)
}

internal fun ApiMovie.toDetail(): MovieDetailDto? = id?.toIntOrNull()?.let { key ->
    MovieDetailDto(key, title.orEmpty(), description, genre.orEmpty(), language.orEmpty(), duration,
        cast.orEmpty(), director, posterUrl, backdropUrl, trailerUrl = trailerUrl, censorRating = censorRating,
        rating = rating, releaseDate = releaseDate, status = status.orEmpty())
}

internal fun ApiEvent.toUi() = id?.takeIf(String::isNotBlank)?.let { key ->
    val minimum = ticketTiers.orEmpty().mapNotNull { it.price }.minOrNull()?.let { java.math.BigDecimal(it).movePointLeft(2) }
        ?: zones.orEmpty().mapNotNull { it.price }.minOrNull()
    Event(key, title.orEmpty(), eventDate(startDate), listOfNotNull(venue, city).filter(String::isNotBlank).joinToString(", "),
        (price ?: minimum)?.toPlainString()?.let { "₹$it" }.orEmpty(), posterUrl ?: bannerUrl, description.orEmpty(), category.orEmpty(),
        startTime?.takeIf(String::isNotBlank)?.take(5) ?: eventTime(startDate),
        endTime?.takeIf(String::isNotBlank)?.take(5) ?: eventTime(endDate),
        remainingCapacity ?: zones?.mapNotNull { it.availableSeats }?.takeIf { it.isNotEmpty() }?.sum())
}

private fun eventTime(value: String?): String = value?.let {
    runCatching { java.time.Instant.parse(it).atZone(java.time.ZoneId.of("Asia/Kolkata")).toLocalTime().toString().take(5) }.getOrNull()
}.orEmpty()

private fun eventDate(value: String?): String = value?.let {
    runCatching { java.time.Instant.parse(it).atZone(java.time.ZoneId.of("Asia/Kolkata")).toLocalDate().toString() }
        .getOrDefault(it.substringBefore('T'))
}.orEmpty()

internal fun Venue.toUi() = id?.takeIf(String::isNotBlank)?.let { key ->
    val minimum = resources.orEmpty().mapNotNull { it.pricePerHour }.minOrNull()
    Turf(key, name.orEmpty(), "", listOfNotNull(address, city).filter(String::isNotBlank).joinToString(", "),
        (basePrice?.toPlainString()?.let { "₹$it" } ?: minimum?.let { "₹${java.math.BigDecimal(it).movePointLeft(2).toPlainString()}" }).orEmpty(), images?.firstOrNull(), description.orEmpty(), rating,
        category ?: resources?.firstOrNull()?.type.orEmpty(), listOfNotNull(category).ifEmpty { resources.orEmpty().mapNotNull { it.type }.distinct() },
        facilities.orEmpty(), basePrice?.toInt() ?: minimum?.div(100) ?: 0, availableDate = "", imageUrls = images.orEmpty(),
        venueId = venueId ?: key, resourceType = resourceType.orEmpty(), reviewCount = reviewCount ?: 0)
}

internal fun User.toUi(city: String = "") = id?.takeIf(String::isNotBlank)?.let {
    UserProfile(it, name ?: username.orEmpty(), email.orEmpty(), phone.orEmpty(), city, createdAt.orEmpty().substringBefore('T'))
}

internal fun com.entrymyslotbe.app.network.model.SponsoredResult.toPromotion(): HomePromotion? {
    val key = campaignId ?: return null
    val content = entity as? Map<*, *> ?: return null
    val title = (content["title"] ?: content["name"]) as? String ?: return null
    val destination = when (entityType?.lowercase()) {
        "movie" -> PromotionDestination.MOVIES
        "event" -> PromotionDestination.EVENTS
        "venue", "turf", "resource" -> PromotionDestination.SPORTS
        else -> return null
    }
    return HomePromotion(key, entityType.orEmpty(), title, (content["description"] as? String).orEmpty(), "View", destination,
        (content["posterUrl"] ?: content["poster_url"] ?: content["banner_url"]) as? String)
}
