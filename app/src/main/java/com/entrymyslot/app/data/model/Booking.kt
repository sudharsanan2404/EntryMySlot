package com.entrymyslot.app.data.model

enum class BookingType { MOVIE, TURF, EVENT }
enum class BookingStatus { UPCOMING, COMPLETED, CANCELLED }

data class Booking(
    val id: String,
    val userId: String,
    val type: BookingType,
    val itemId: String,
    val venueId: String? = null,
    val dateTime: String,
    val details: String,
    val price: String,
    val status: BookingStatus,
    val bookingReference: String = id,
    val title: String = "Booking",
    val location: String = ""
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val city: String,
    val memberSince: String
)

enum class PaymentMethodType { UPI, CARD, NET_BANKING }

data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val name: String,
    val description: String,
    val badge: String? = null
)

data class BookingDetails(
    val itemId: String,
    val title: String,
    val category: BookingType,
    val date: String,
    val time: String,
    val location: String,
    val details: String,
    val imageUrl: String? = null,
    val baseAmount: Int,
    val convenienceFee: Int,
    val taxes: Int,
    val bookingFee: Int = 0,
    val serviceCharge: Int = 0,
    val payableAmount: Int = baseAmount + convenienceFee + taxes
)

data class TicketDetails(
    val bookingId: String,
    val ticketUuid: String = bookingId,
    val title: String,
    val category: String,
    val venue: String,
    val location: String = "",
    val date: String,
    val time: String,
    val admission: String,
    val amount: String,
    val screenType: String? = null,
    val language: String? = null,
    val format: String? = null,
    val screenName: String? = null,
    val seatTier: String? = null,
    val ticketCount: Int? = null,
    val ticketTier: String? = null,
    val encodedQrPayload: String? = null
) {
    val slots: List<String>
        get() = time.split(" - ", ",").map(String::trim).filter(String::isNotEmpty)
    val qrPayload: String
        get() = encodedQrPayload ?: ticketUuid
}
