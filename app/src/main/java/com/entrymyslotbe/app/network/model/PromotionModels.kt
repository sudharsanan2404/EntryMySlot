package com.entrymyslotbe.app.network.model

// ========== PROMOTION MODELS ==========
data class PromotionPackage(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val placementType: String? = null,   // home_top, search_top, category_top, etc.
    val price: Int? = null,
    val currency: String = "INR",
    val durationDays: Int? = null,
    val maxImpressions: Int? = null,
    val maxClicks: Int? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null
)

data class Campaign(
    val id: String? = null,
    val name: String? = null,
    val organizationId: String? = null,
    val packageId: String? = null,
    val packageName: String? = null,
    val targetEntityId: String? = null,
    val targetEntityType: String? = null,  // event, movie, venue
    val status: String? = null,  // pending, active, paused, completed, cancelled
    val startDate: String? = null,
    val endDate: String? = null,
    val impressions: Int = 0,
    val clicks: Int = 0,
    val spent: Int? = null,
    val budget: Int? = null,
    val createdAt: String? = null
)

data class CreateCampaignRequest(
    val packageId: String,
    val targetEntityId: String,
    val targetEntityType: String,
    val budget: Int? = null
)

data class CampaignAnalytics(
    val campaignId: String? = null,
    val impressions: Int = 0,
    val clicks: Int = 0,
    val ctr: Double = 0.0,
    val spent: Int = 0,
    val dailyStats: List<DailyStat>? = null
)

data class DailyStat(
    val date: String? = null,
    val impressions: Int = 0,
    val clicks: Int = 0
)

data class SponsoredResultsRequest(
    val placement: String,
    val category: String? = null,
    val locationKey: String? = null,
    val entityType: String? = null,
    val limit: Int = 5
)

data class SponsoredResult(
    val campaignId: String? = null,
    val entityId: String? = null,
    val entityType: String? = null,
    val entity: Any? = null,  // Movie or Event or Venue
    val score: Int? = null
)

data class SponsoredResults(val sponsored: List<SponsoredResult> = emptyList(), val total: Int = 0)

data class TrackClickRequest(
    val campaignId: String,
    val entityId: String,
    val entityType: String
)
