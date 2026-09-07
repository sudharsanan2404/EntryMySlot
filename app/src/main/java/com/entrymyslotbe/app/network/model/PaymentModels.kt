package com.entrymyslotbe.app.network.model

data class PaymentOrderRequest(
    val bookingId: String,
    val bookingType: String,    // movie, event, turf
    val amount: Int,
    val customerEmail: String,
    val customerPhone: String
)

data class PaymentOrderResponse(
    val order: PaymentOrderResponse? = null,
    @com.google.gson.annotations.SerializedName(value = "orderId", alternate = ["order_id"])
    val orderId: String? = null,
    @com.google.gson.annotations.SerializedName(value = "orderAmount", alternate = ["amount"])
    val orderAmount: java.math.BigDecimal? = null,
    val currency: String? = null,
    val paymentSessionId: String? = null,
    val paymentUrl: String? = null
)

data class PaymentVerifyRequest(
    val bookingId: String,
    val gatewayOrderId: String,
    val gatewayPaymentId: String
)

data class WebhookPayload(
    val type: String? = null,
    val data: Map<String, Any>? = null
)

data class LayoutVersion(
    val id: String? = null,
    val screenId: String? = null,
    val name: String? = null,
    val seatData: List<LayoutSeat>? = null,
    val isCurrent: Boolean = false,
    val createdAt: String? = null
)

data class LayoutSeat(
    val row: String? = null,
    val number: String? = null,
    val type: String? = null,  // standard, premium, sofa, couple, wheelchair
    val x: Int? = null,
    val y: Int? = null
)
