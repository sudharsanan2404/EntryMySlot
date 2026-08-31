package com.entrymyslot.app.data.model

data class Event(
    override val id: String,
    override val title: String,
    override val date: String,
    override val location: String,
    override val price: String,
    override val imageUrl: String? = null,
    val description: String,
    val category: String,
    val time: String,
    val interested: Boolean = false
) : CatalogItem

data class TicketTier(
    val id: String,
    val eventId: String,
    val name: String,
    val price: Int,
    val description: String,
    val available: Int,
    val isSoldOut: Boolean = false
)

enum class NotificationKind { REMINDER, BOOKING, OFFER }

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val kind: NotificationKind
)

enum class PromotionDestination { MOVIES, SPORTS, EVENTS }

data class HomePromotion(
    val id: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val cta: String,
    val destination: PromotionDestination,
    val imageUrl: String? = null
)
