package com.entrymyslotbe.app.network.model

data class Admin(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val role: String? = null,  // super_admin, admin, event_manager, ticket_scanner
    val permissions: Map<String, Boolean>? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null
)

data class AdminLoginRequest(
    val email: String,
    val password: String
)

data class AdminLoginResponse(
    val admin: Admin? = null,
    val token: String? = null,
    val accessToken: String? = null,
    val permissions: Map<String, Boolean>? = null
) {
    fun resolvedAccessToken(): String? = accessToken ?: token
}

data class AdminStats(
    val totalUsers: Int? = null,
    val totalBookings: Int? = null,
    val totalRevenue: Int? = null,
    val totalEvents: Int? = null,
    val totalMovies: Int? = null,
    val totalVenues: Int? = null,
    val pendingReviews: Int? = null
)

data class Refund(
    val id: String? = null,
    val bookingId: String? = null,
    val bookingType: String? = null,
    val amount: Int? = null,
    val currency: String = "INR",
    val reason: String? = null,
    val status: String? = null,  // pending, processed, completed, failed
    val processedBy: String? = null,
    val createdAt: String? = null
)

data class CreateRefundRequest(
    val bookingId: String,
    val bookingType: String,
    val amount: Int,
    val reason: String
)

data class AuditLog(
    val id: String? = null,
    val adminId: String? = null,
    val adminEmail: String? = null,
    val action: String? = null,
    val targetType: String? = null,
    val targetId: String? = null,
    val details: Map<String, Any>? = null,
    val ipAddress: String? = null,
    val createdAt: String? = null
)
