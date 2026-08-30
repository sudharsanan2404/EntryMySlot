package com.entrymyslot.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────
// Event list item   (GET /events)
// ──────────────────────────────────────────────

@Serializable
data class EventDto(
    val id: Long,
    val title: String,

    @SerialName("event_date")
    val eventDate: String? = null,

    @SerialName("start_at")
    val startAt: String? = null,

    val venue: String? = null,
    val city: String? = null,
    val price: String? = null,

    @SerialName("is_free")
    val isFree: Boolean = false,

    val category: String? = null,
    val description: String? = null,

    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerialName("banner_url")
    val bannerUrl: String? = null
)

// ──────────────────────────────────────────────
// Event detail response   (GET /events/:id)
// ──────────────────────────────────────────────

@Serializable
data class EventDetailResponse(
    val id: Long,
    val title: String,

    @SerialName("event_date")
    val eventDate: String? = null,

    @SerialName("start_at")
    val startAt: String? = null,

    val venue: String? = null,
    val city: String? = null,
    val price: String? = null,

    @SerialName("is_free")
    val isFree: Boolean = false,

    val category: String? = null,
    val description: String? = null,

    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerialName("banner_url")
    val bannerUrl: String? = null,

    val capacity: Int? = null,

    @SerialName("ticket_price")
    val ticketPrice: String? = null,

    val currency: String? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("organizer_name")
    val organizerName: String? = null,

    @SerialName("contact_email")
    val contactEmail: String? = null,

    @SerialName("contact_phone")
    val contactPhone: String? = null,

    @SerialName("age_restriction")
    val ageRestriction: Int? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class EventStats(
    val booked: Int? = null,
    val capacity: Int? = null
)

@Serializable
data class EventDetailData(
    val event: EventDetailResponse,
    val stats: EventStats? = null,
    val related: List<EventDto> = emptyList()
)

@Serializable
data class EventDetailWrapper(
    val success: Boolean,
    val data: EventDetailData? = null
)
