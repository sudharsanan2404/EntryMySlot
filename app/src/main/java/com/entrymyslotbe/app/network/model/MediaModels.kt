package com.entrymyslotbe.app.network.model

// ========== MEDIA / UPLOAD MODELS ==========
data class Media(
    val id: String? = null,
    val filename: String? = null,
    val originalName: String? = null,
    val mimeType: String? = null,
    val size: Long? = null,
    val url: String? = null,
    val sha256: String? = null,
    val uploadedBy: String? = null,
    val createdAt: String? = null
)

data class EventMedia(
    val id: String? = null,
    val eventId: String? = null,
    val mediaId: String? = null,
    val sortOrder: Int? = null,
    val isPrimary: Boolean = false,
    val caption: String? = null,
    val media: Media? = null
)

data class Banner(
    val id: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val deepLink: String? = null,
    val placement: String? = null,
    val priority: Int = 0,
    val isActive: Boolean = false,
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: String? = null
)

data class TicketAdBanner(
    val id: String? = null,
    val banner: Banner? = null,
    val activeBanner: Banner? = null
)

data class UploadMediaRequest(
    val base64Data: String,
    val filename: String
)
