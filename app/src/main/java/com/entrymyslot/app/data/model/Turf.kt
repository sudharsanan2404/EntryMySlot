package com.entrymyslot.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────
// Sports Venue   (GET /grounds)
// ──────────────────────────────────────────────

@Serializable
data class SportsVenueDto(
    val id: Long,
    val name: String,
    val slug: String? = null,

    @SerialName("venue_type")
    val venueType: String,

    val city: String? = null,
    val address: String? = null,

    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerialName("banner_url")
    val bannerUrl: String? = null,

    val description: String? = null,
    val amenities: List<String> = emptyList(),

    @SerialName("is_active")
    val isActive: Boolean = true,

    val latitude: Double? = null,
    val longitude: Double? = null,

    @SerialName("contact_phone")
    val contactPhone: String? = null,

    val rating: Double? = null,

    @SerialName("review_count")
    val reviewCount: Int? = null
)

// ──────────────────────────────────────────────
// Resource (turf type)   (GET /grounds/:id/resources)
// ──────────────────────────────────────────────

@Serializable
data class ResourceDto(
    val id: Long,

    @SerialName("venue_id")
    val venueId: Long,

    val name: String,
    val description: String? = null,

    val category: String? = null,

    @SerialName("unit_price")
    val unitPrice: String,

    val currency: String? = null,

    @SerialName("max_units")
    val maxUnits: Int,

    @SerialName("duration_hours")
    val durationHours: Int = 1,

    @SerialName("is_active")
    val isActive: Boolean = true,

    val images: List<String> = emptyList(),

    val amenities: List<String> = emptyList()
)

// ──────────────────────────────────────────────
// Availability slot   (GET /resources/:id/availability)
// ──────────────────────────────────────────────

@Serializable
data class AvailabilitySlotDto(
    val date: String,
    val slots: List<TimeSlotDto> = emptyList()
)

@Serializable
data class TimeSlotDto(
    @SerialName("start_time")
    val startTime: String,

    @SerialName("end_time")
    val endTime: String,

    @SerialName("available_units")
    val availableUnits: Int = 0,

    @SerialName("total_units")
    val totalUnits: Int = 0,

    val price: String? = null
)

@Serializable
data class AvailabilityData(
    @SerialName("resource_id")
    val resourceId: Long,

    @SerialName("resource_name")
    val resourceName: String? = null,

    val slots: List<AvailabilitySlotDto> = emptyList()
)

@Serializable
data class AvailabilityWrapper(
    val success: Boolean,
    val data: AvailabilityData? = null
)

// ──────────────────────────────────────────────
// Turf review   (GET /grounds/:id/reviews)
// ──────────────────────────────────────────────

@Serializable
data class TurfReviewDto(
    val id: Long,

    @SerialName("venue_id")
    val venueId: Long,

    @SerialName("user_id")
    val userId: Long,

    @SerialName("user_name")
    val userName: String? = null,

    val rating: Int,
    val comment: String? = null,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class TurfReviewsData(
    @SerialName("venue_id")
    val venueId: Long,

    @SerialName("average_rating")
    val averageRating: Double,

    @SerialName("total_reviews")
    val totalReviews: Int,

    val reviews: List<TurfReviewDto> = emptyList()
)

@Serializable
data class TurfReviewsWrapper(
    val success: Boolean,
    val data: TurfReviewsData? = null
)