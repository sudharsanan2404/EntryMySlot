package com.entrymyslot.app.data.model

data class Turf(
    override val id: String,
    override val title: String,
    override val date: String,
    override val location: String,
    override val price: String,
    override val imageUrl: String? = null,
    val description: String,
    val rating: Double,
    val venueType: String,
    val sports: List<String>,
    val facilities: List<String>,
    val pricePerHour: Int,
    val availableDate: String = "2026-08-31",
    val imageUrls: List<String> = emptyList(),
    val specifications: List<Pair<String, String>> = emptyList(),
    val rules: List<String> = emptyList(),
    val venueId: String? = null,
    val resourceType: String = "slot_based",
    val reviewCount: Int = 0
) : CatalogItem

data class TurfSlot(
    val turfId: String,
    val date: String,
    val hour: Int,
    val time: String,
    val booked: Boolean
)
