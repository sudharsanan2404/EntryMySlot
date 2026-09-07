package com.entrymyslotbe.app.network.model

import com.google.gson.annotations.SerializedName

// ========== TURF MODELS ==========
data class Venue(
    val id: String? = null,
    @SerializedName("venue_id") val venueId: String? = null,
    @SerializedName("resource_type") val resourceType: String? = null,
    val category: String? = null,
    @SerializedName("base_price") val basePrice: java.math.BigDecimal? = null,
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val email: String? = null,
    val images: List<String>? = null,
    @SerializedName(value = "facilities", alternate = ["amenities"])
    val facilities: List<String>? = null,
    val status: String? = null,  // active, inactive
    @SerializedName(value = "rating", alternate = ["avg_rating"])
    val rating: Double? = null,
    @SerializedName(value = "reviewCount", alternate = ["review_count"])
    val reviewCount: Int? = null,
    val resources: List<Resource>? = null,
    @SerializedName(value = "organizationId", alternate = ["organization_id"])
    val organizationId: String? = null,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null
)

data class Resource(
    val id: String? = null,
    val venueId: String? = null,
    val name: String? = null,
    val type: String? = null,     // cricket, football, badminton, etc.
    val description: String? = null,
    val pricePerHour: Int? = null,
    val currency: String = "INR",
    val durationOptions: List<Int>? = null,
    val maxPlayers: Int? = null,
    val status: String? = null
)

data class ResourceSlot(
    val id: String? = null,
    val resourceId: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String? = null,   // available, booked, blocked
    val price: Int? = null
)

data class ResourceAvailability(
    val resource_id: String? = null,
    val resource_name: String? = null,
    val venue_id: String? = null,
    val venue_name: String? = null,
    val date: String? = null,
    val timezone: String? = null,
    val slots: List<CustomerTurfSlot> = emptyList(),
    val summary: TurfAvailabilitySummary? = null,
)

data class CustomerTurfSlot(
    val unit_id: String? = null,
    val starts_at: String? = null,
    val ends_at: String? = null,
    val status: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val formatted_time: String? = null,
    val duration_minutes: Int? = null,
    val blocked_reason: String? = null,
)

data class TurfAvailabilitySummary(
    val available: Int = 0,
    val held: Int = 0,
    val booked: Int = 0,
    val blocked: Int = 0,
    val unavailable: Int = 0,
)

data class TurfBookingRequest(
    val availability_unit_id: Long,
    val quantity: Int = 1,
    val booking_type: String = "online",
    val coupon_code: String? = null,
    val amount: Double? = null,
    val duration_hours: Double? = null,
)

data class TurfBookingResponse(
    val booking: TurfBooking? = null,
    val couponDiscount: Double? = null,
    val correlationId: String? = null,
    val idempotent: Boolean = false,
)

data class TurfBooking(
    val id: String? = null,
    @SerializedName(value = "bookingReference", alternate = ["booking_reference"])
    val bookingReference: String? = null,
    @SerializedName(value = "venueId", alternate = ["venue_id"])
    val venueId: String? = null,
    @SerializedName(value = "venueName", alternate = ["venue_name"])
    val venueName: String? = null,
    @SerializedName(value = "resourceId", alternate = ["resource_id"])
    val resourceId: String? = null,
    @SerializedName(value = "resourceName", alternate = ["resource_name"])
    val resourceName: String? = null,
    val date: String? = null,
    @SerializedName(value = "timeSlot", alternate = ["time_slot"])
    val timeSlot: String? = null,
    @SerializedName(value = "customerName", alternate = ["customer_name"])
    val customerName: String? = null,
    @SerializedName(value = "customerEmail", alternate = ["customer_email"])
    val customerEmail: String? = null,
    @SerializedName(value = "customerPhone", alternate = ["customer_phone"])
    val customerPhone: String? = null,
    val status: String? = null,   // pending, confirmed, completed, canceled
    @SerializedName(value = "totalAmount", alternate = ["total_amount", "amount"])
    val totalAmount: java.math.BigDecimal? = null,
    @SerializedName(value = "qrData", alternate = ["qr_data", "qr_code"]) val qrData: String? = null,
    @SerializedName("qr_token") val qrToken: String? = null,
    @SerializedName(value = "startsAt", alternate = ["starts_at", "slot_start"]) val startsAt: String? = null,
    @SerializedName(value = "endsAt", alternate = ["ends_at", "slot_end"]) val endsAt: String? = null,
    val currency: String? = null,
    val notes: String? = null,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null
)

data class Review(
    val id: String? = null,
    val bookingId: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val rating: Int? = null,
    val comment: String? = null,
    val createdAt: String? = null
)

data class TurfBookingPage(
    val items: List<TurfBooking> = emptyList(), val total: Int = 0,
    val page: Int = 1, val pageSize: Int = 20, val totalPages: Int = 1
)

data class Coupon(
    val id: String? = null,
    val code: String? = null,
    val description: String? = null,
    val discountType: String? = null, // percentage, fixed
    val discountValue: Int? = null,
    val minAmount: Int? = null,
    val maxDiscount: Int? = null,
    val validFrom: String? = null,
    val validUntil: String? = null,
    val usageLimit: Int? = null,
    val usedCount: Int? = null,
    val isActive: Boolean = true
)

data class Settlement(
    val id: String? = null,
    val bookingId: String? = null,
    val amount: Int? = null,
    val currency: String = "INR",
    val status: String? = null,   // pending, processed, completed
    val settlementDate: String? = null,
    val createdAt: String? = null
)
