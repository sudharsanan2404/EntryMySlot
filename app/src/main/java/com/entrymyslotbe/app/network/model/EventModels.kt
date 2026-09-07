package com.entrymyslotbe.app.network.model
import com.google.gson.annotations.SerializedName

// ========== EVENT MODELS ==========
data class Event(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val city: String? = null,
    val venue: String? = null,
    val address: String? = null,
    @SerializedName(value = "startDate", alternate = ["start_at"])
    val startDate: String? = null,
    @SerializedName(value = "endDate", alternate = ["end_at"])
    val endDate: String? = null,
    @SerializedName(value = "posterUrl", alternate = ["thumbnail_url"])
    val posterUrl: String? = null,
    @SerializedName(value = "bannerUrl", alternate = ["banner_url"])
    val bannerUrl: String? = null,
    val price: java.math.BigDecimal? = null,
    val currency: String? = null,
    @SerializedName("remaining_capacity") val remainingCapacity: Int? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    val status: String? = null,  // draft, published, live, sold_out, cancelled, archived
    val isFeatured: Boolean = false,
    val organizerId: String? = null,
    val organizerName: String? = null,
    val ticketTiers: List<TicketTier>? = null,
    val zones: List<Zone>? = null,
    val createdAt: String? = null
)

data class TicketTier(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Int? = null,
    val currency: String = "INR",
    val totalQuantity: Int? = null,
    val availableQuantity: Int? = null,
    val minPerOrder: Int = 1,
    val maxPerOrder: Int = 10
)

data class Zone(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val price: java.math.BigDecimal? = null,
    val currency: String? = null,
    @SerializedName(value = "totalSeats", alternate = ["total_capacity"])
    val totalSeats: Int? = null,
    @SerializedName(value = "availableSeats", alternate = ["remaining_capacity"])
    val availableSeats: Int? = null,
    @SerializedName(value = "colorCode", alternate = ["color"])
    val colorCode: String? = null,
    val rows: List<ZoneRow>? = null
)

// Zone controllers intentionally do not use the platform's standard {success,data}
// envelope. Keep these wrappers explicit so UI integration cannot silently fail.
data class ZonesResponse(val zones: List<Zone> = emptyList())
data class ZoneResponse(val zone: Zone? = null)

data class ZoneRow(
    val rowLabel: String? = null,
    val seats: List<ZoneSeat>? = null
)

data class ZoneSeat(
    val id: String? = null,
    val number: String? = null,
    val status: String? = null  // available, booked, reserved
)

data class EventFilters(
    val city: String? = null,
    val category: String? = null,
    val status: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20
)

data class EventStats(
    val totalBookings: Int? = null,
    val totalCheckIns: Int? = null,
    val totalRevenue: Int? = null,
    val availableSeats: Int? = null,
    val bookedSeats: Int? = null
)

data class EventAttendee(
    val full_name: String,
    val phone: String,
    val email: String? = null,
    val age: Int? = null,
    val gender: String? = null
)

/** Exact payload consumed by bookingController.createBooking on the test server. */
data class EventBookingRequest(
    val event_id: String,
    val attendees: List<EventAttendee>,
    val zone_id: String? = null,
    val ticket_tier_id: String? = null
)

data class EventBookingResponse(
    val bookingId: String? = null,
    val ticketCount: Int? = null,
    val status: String? = null,
    val tickets: List<EventIssuedTicket> = emptyList(),
    val zone: EventSelectedZone? = null,
    val payment: EventBookingPayment? = null,
)

data class EventIssuedTicket(
    val ticketUuid: String? = null,
    val attendeeName: String? = null,
    val attendeePhone: String? = null,
    val signature: String? = null,
)

data class EventSelectedZone(
    val zoneId: String? = null,
    val zoneName: String? = null,
    val unitPricePaise: Long? = null,
)

data class EventBookingPayment(
    val orderId: String? = null,
    val amount: Long? = null,
    val currency: String? = null,
    val paymentSessionId: String? = null,
)

data class EventBookingDetails(
    val booking: EventBooking? = null,
    val tickets: List<Ticket> = emptyList(),
)

data class EventBooking(
    @SerializedName(value = "id", alternate = ["bookingId"])
    val id: String? = null,
    @SerializedName(value = "bookingReference", alternate = ["booking_reference"])
    val bookingReference: String? = null,
    @SerializedName(value = "eventId", alternate = ["event_id"])
    val eventId: String? = null,
    @SerializedName(value = "eventTitle", alternate = ["event_title"])
    val eventTitle: String? = null,
    val status: String? = null,
    val attendeeName: String? = null,
    val attendeePhone: String? = null,
    val attendeeEmail: String? = null,
    @SerializedName(value = "ticketCount", alternate = ["ticket_count"])
    val ticketCount: Int? = null,
    @SerializedName(value = "totalAmount", alternate = ["total_amount", "amount"])
    val totalAmount: java.math.BigDecimal? = null,
    @SerializedName(value = "event_date", alternate = ["event_start_at"]) val eventDate: String? = null,
    @SerializedName(value = "venue", alternate = ["event_venue"]) val venue: String? = null,
    val currency: String? = null,
    val tickets: List<Ticket>? = null,
    val createdAt: String? = null
)

// Categories and Cities reference
data class Category(
    val id: String? = null,
    val name: String? = null,
    val icon: String? = null
)

data class City(
    val id: String? = null,
    val name: String? = null,
    val state: String? = null,
    val eventCount: Int? = null
)
