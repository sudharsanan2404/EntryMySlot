package com.entrymyslot.app.screens.payment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.BookingDetails
import com.entrymyslot.app.data.model.BookingType
import com.entrymyslot.app.data.model.PaymentMethod
import com.entrymyslot.app.data.model.PaymentMethodType
import kotlin.math.roundToInt

private val PaymentOrange = Color(0xFFFA580B)
private val PaymentOrangeDark = Color(0xFF9B3100)
private val PaymentOrangeDeep = Color(0xFF4C1A08)
private val PaymentWhite = Color.White
private val PaymentGray = Color(0xFF98A2B3)
private val PaymentCard = Color(0xFF0E1739).copy(alpha = .94f)
private val PaymentCardLight = Color(0xFF1B2854)
private val PaymentBorder = Color(0xFF31426F).copy(alpha = .72f)
private val PaymentDivider = Color.White.copy(alpha = .09f)
private val PaymentGreen = Color(0xFF20C66B)
private val PaymentMint = Color(0xFFB7F7D2)

typealias BookingCategory = BookingType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    bookingDetails: BookingDetails,
    onBackClick: () -> Unit = {},
    onPaySuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedMethodId by remember { mutableStateOf("upi") }
    var paymentSuccessful by rememberSaveable { mutableStateOf(false) }
    var paymentProcessing by rememberSaveable { mutableStateOf(false) }
    var paymentError by remember { mutableStateOf<String?>(null) }

    val ticketPrice = bookingDetails.baseAmount
    val convenienceFee = bookingDetails.convenienceFee
    val taxes = bookingDetails.taxes
    val totalAmount = ticketPrice + convenienceFee + taxes
    val methods = FakeData.paymentMethods
    val upiPaymentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        paymentProcessing = false
        if (result.resultCode == Activity.RESULT_OK && upiPaymentSucceeded(result.data)) {
            paymentSuccessful = true
            paymentError = null
        } else {
            paymentError = "Payment was not completed. Please try again."
        }
    }
    val launchUpiPayment = {
        if (selectedMethodId != "upi") {
            paymentError = "Select UPI to complete payment using a UPI app."
        } else {
            val paymentUri = Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", "entrymyslot@upi")
                .appendQueryParameter("pn", "EntryMySlot")
                .appendQueryParameter("tn", bookingDetails.title)
                .appendQueryParameter("am", totalAmount.toString())
                .appendQueryParameter("cu", "INR")
                .build()
            val upiIntent = Intent(Intent.ACTION_VIEW, paymentUri)
            try {
                paymentProcessing = true
                paymentError = null
                upiPaymentLauncher.launch(Intent.createChooser(upiIntent, "Pay with UPI"))
            } catch (_: ActivityNotFoundException) {
                paymentProcessing = false
                paymentError = "No UPI app was found on this device."
            }
        }
    }

    if (paymentSuccessful) {
        PaymentSuccessfulScreen(
            bookingDetails = bookingDetails,
            totalAmount = totalAmount,
            paymentMethod = methods.firstOrNull { it.id == selectedMethodId }?.name ?: "Online payment",
            onViewTicket = onPaySuccess
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment", color = PaymentWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = PaymentWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            PaymentFloatingBar(
                amount = totalAmount,
                enabled = !paymentProcessing,
                processing = paymentProcessing,
                error = paymentError,
                onPayClick = launchUpiPayment
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            GlowBackground()
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 26.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                item { BookingSummaryCard(bookingDetails) }
                item {
                    PaymentOptionsSection(methods, selectedMethodId) {
                        selectedMethodId = it
                        paymentError = null
                    }
                }
                item {
                    OrderSummaryCard(
                        bookingDetails = bookingDetails,
                        ticketPrice = ticketPrice,
                        convenienceFee = convenienceFee,
                        taxes = taxes,
                        totalAmount = totalAmount
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeading("Notes")
                        NonRefundableNotice(bookingDetails.category)
                    }
                }
                item { SecurityNote() }
            }
        }
    }
}

@Composable
private fun BookingSummaryCard(details: BookingDetails) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PaymentCard),
        border = BorderStroke(1.dp, PaymentBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF173A7D).copy(alpha = .38f), Color.Transparent)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(14.dp)).background(PaymentCardLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(bookingIcon(details.category), null, tint = PaymentOrange, modifier = Modifier.size(31.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "PAYING FOR ${bookingLabel(details.category)}",
                    color = PaymentOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = .8.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    details.title,
                    color = PaymentWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 21.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(details.location, color = PaymentGray, fontSize = 12.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarToday, null, tint = PaymentGray, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("${details.date}  •  ${details.time}", color = PaymentWhite.copy(alpha = .88f), fontSize = 11.sp)
                }
                Text(
                    details.details,
                    color = PaymentOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentOptionsSection(methods: List<PaymentMethod>, selectedId: String, onSelect: (String) -> Unit) {
    Column {
        SectionHeading("Payment options")
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = PaymentCard,
            border = BorderStroke(1.dp, PaymentBorder),
            shadowElevation = 8.dp
        ) {
            Column {
                methods.forEachIndexed { index, method ->
                    PaymentOptionRow(method, method.id == selectedId) { onSelect(method.id) }
                    if (index != methods.lastIndex) HorizontalDivider(color = PaymentDivider)
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(method: PaymentMethod, isSelected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, color = if (isSelected) PaymentOrange.copy(alpha = .10f) else Color.Transparent) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionCircle(isSelected)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(method.name, color = PaymentWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    method.badge?.let { badge ->
                        Surface(color = PaymentOrange.copy(alpha = .15f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                badge,
                                color = PaymentOrange,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(7.dp))
                    }
                    Text(method.description, color = PaymentGray, fontSize = 10.sp, maxLines = 1)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                paymentMethodIcon(method.type),
                contentDescription = null,
                tint = if (isSelected) PaymentOrange else PaymentGray,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun SelectionCircle(isSelected: Boolean) {
    Box(
        modifier = Modifier.size(21.dp).border(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) PaymentOrange else PaymentGray,
            CircleShape
        ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(PaymentOrange))
        }
    }
}

@Composable
private fun OrderSummaryCard(
    bookingDetails: BookingDetails,
    ticketPrice: Int,
    convenienceFee: Int,
    taxes: Int,
    totalAmount: Int
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PaymentCard),
        border = BorderStroke(1.dp, PaymentBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("ORDER SUMMARY", color = PaymentGray, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(bookingDetails.title, color = PaymentWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                "${bookingDetails.location} • ${bookingDetails.date} • ${bookingDetails.time}",
                color = PaymentGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(17.dp))
            SummaryRow(summaryItemLabel(bookingDetails.category), "₹$ticketPrice")
            SummaryRow("Convenience fee", "₹$convenienceFee")
            SummaryRow("Taxes", "₹$taxes")
            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = PaymentDivider)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Amount payable", color = PaymentWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("₹$totalAmount", color = PaymentOrange, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun PaymentFloatingBar(
    amount: Int,
    enabled: Boolean,
    processing: Boolean,
    error: String?,
    onPayClick: () -> Unit
) {
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(PaymentCard.copy(alpha = .98f))
                .border(1.dp, PaymentGreen.copy(alpha = .30f), RoundedCornerShape(24.dp))
                .shadow(18.dp, RoundedCornerShape(24.dp))
                .padding(11.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (processing) "Waiting for UPI" else "Amount payable", color = PaymentGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text("₹$amount", color = PaymentGreen, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            }
            if (error != null) {
                Text(error, color = Color(0xFFFF7A7A), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp))
            }
            SwipeToPay(
                amount = amount,
                enabled = enabled,
                processing = processing,
                onSwipeComplete = onPayClick
            )
        }
    }
}

@Composable
private fun SwipeToPay(
    amount: Int,
    enabled: Boolean,
    processing: Boolean = false,
    onSwipeComplete: () -> Unit
) {
    val density = LocalDensity.current
    val thumbSize = 52.dp
    val trackPadding = 6.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (enabled || processing) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF052E22), Color(0xFF0A4933))
                    )
                } else {
                    Brush.horizontalGradient(listOf(PaymentCardLight, PaymentCardLight))
                }
            )
            .border(
                1.dp,
                if (enabled || processing) PaymentGreen.copy(alpha = .68f) else PaymentBorder,
                RoundedCornerShape(22.dp)
            )
            .semantics {
                onClick(label = "Pay ₹$amount") {
                    if (enabled) onSwipeComplete()
                    enabled
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val maxOffsetPx = with(density) {
            (maxWidth - thumbSize - (trackPadding * 2)).toPx().coerceAtLeast(0f)
        }
        var dragOffsetPx by remember(maxOffsetPx, enabled) { mutableFloatStateOf(0f) }
        val progressWidth = with(density) {
            (dragOffsetPx + thumbSize.toPx()).toDp()
        }
        val maxProgressWidth = maxWidth - (trackPadding * 2)

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = trackPadding)
                .width(progressWidth.coerceAtMost(maxProgressWidth))
                .height(thumbSize)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    if (enabled || processing) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF08753E), Color(0xFF16A85B), Color(0xFF32D978))
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp, end = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (enabled || processing) PaymentMint.copy(alpha = .90f) else PaymentGray,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = when {
                    processing -> "Waiting for UPI payment…"
                    enabled -> "Swipe to pay ₹$amount"
                    else -> "Select a payment method"
                },
                color = if (enabled || processing) PaymentMint else PaymentGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .padding(start = trackPadding)
                .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
                .size(thumbSize)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = PaymentGreen.copy(alpha = .55f),
                    spotColor = PaymentGreen.copy(alpha = .75f)
                )
                .clip(CircleShape)
                .background(
                    if (enabled || processing) {
                        Brush.linearGradient(listOf(Color(0xFF34D978), Color(0xFF16A34A)))
                    } else {
                        Brush.linearGradient(listOf(PaymentGray.copy(alpha = .65f), PaymentGray.copy(alpha = .48f)))
                    }
                )
                .border(1.dp, if (enabled) Color.White.copy(alpha = .28f) else Color.Transparent, CircleShape)
                .pointerInput(enabled, maxOffsetPx) {
                    if (enabled) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(0f, maxOffsetPx)
                            },
                            onDragEnd = {
                                if (dragOffsetPx >= maxOffsetPx * .72f) {
                                    dragOffsetPx = maxOffsetPx
                                    onSwipeComplete()
                                } else {
                                    dragOffsetPx = 0f
                                }
                            },
                            onDragCancel = { dragOffsetPx = 0f }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (processing) {
                CircularProgressIndicator(color = PaymentWhite, strokeWidth = 2.5.dp, modifier = Modifier.size(23.dp))
            } else {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Swipe payment handle",
                    tint = PaymentWhite,
                    modifier = Modifier.size(27.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentSuccessfulScreen(
    bookingDetails: BookingDetails,
    totalAmount: Int,
    paymentMethod: String,
    onViewTicket: () -> Unit
) {
    val reference = remember(bookingDetails.title) {
        bookingDetails.title.hashCode().toUInt().toString(16).uppercase().padStart(8, '0').take(8)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF041D19), Color(0xFF082B24), Color(0xFF06172C)),
                    start = Offset.Zero,
                    end = Offset(1000f, 1800f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .shadow(12.dp, CircleShape, ambientColor = PaymentGreen, spotColor = PaymentGreen)
                    .clip(CircleShape)
                    .background(PaymentGreen.copy(alpha = .14f))
                    .border(1.dp, PaymentGreen.copy(alpha = .55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = PaymentGreen,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "Payment successful!",
                color = PaymentWhite,
                fontSize = 25.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your ${bookingItemName(bookingDetails.category).lowercase()} is confirmed.",
                color = PaymentMint.copy(alpha = .82f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Surface(
                modifier = Modifier.padding(top = 12.dp),
                shape = RoundedCornerShape(50),
                color = PaymentGreen.copy(alpha = .12f),
                border = BorderStroke(1.dp, PaymentGreen.copy(alpha = .34f))
            ) {
                Text(
                    "PAID · ₹$totalAmount",
                    color = PaymentGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2926).copy(alpha = .94f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = .10f))
            ) {
                Column(Modifier.padding(17.dp)) {
                    Text("PAYMENT RECEIPT", color = PaymentGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(11.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                                .background(PaymentGreen.copy(alpha = .12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(bookingIcon(bookingDetails.category), null, tint = PaymentGreen, modifier = Modifier.size(21.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 11.dp)) {
                            Text(bookingDetails.title, color = PaymentWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                            Text(bookingLabel(bookingDetails.category), color = PaymentMint.copy(alpha = .68f), fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Color.White.copy(alpha = .08f))
                    SuccessDetailRow("Amount paid", "₹$totalAmount", emphasize = true)
                    SuccessDetailRow("Paid using", paymentMethod)
                    SuccessDetailRow("Reference", "EMS-$reference")
                }
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onViewTicket,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaymentGreen,
                    contentColor = PaymentWhite
                )
            ) {
                Icon(Icons.Rounded.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(9.dp))
                Text("View ticket", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "A confirmation has been added to My Bookings",
                color = PaymentGray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SuccessDetailRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = PaymentGray, fontSize = 12.sp)
        Text(
            value,
            color = if (emphasize) PaymentGreen else PaymentWhite,
            fontSize = if (emphasize) 17.sp else 12.sp,
            fontWeight = if (emphasize) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PaymentGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(value, color = PaymentWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun NonRefundableNotice(category: BookingCategory) {
    Surface(
        color = PaymentOrange.copy(alpha = .09f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, PaymentOrange.copy(alpha = .38f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Info, null, tint = PaymentOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "${bookingItemName(category)} is non-refundable",
                    color = PaymentWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    "Once payment is completed, this booking cannot be cancelled, exchanged, or refunded.",
                    color = PaymentGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SecurityNote() {
    Row(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Lock, null, tint = PaymentOrange, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(7.dp))
        Text("100% secure and encrypted payment", color = PaymentGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(text, color = PaymentWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

private fun bookingIcon(category: BookingCategory): ImageVector = when (category) {
    BookingCategory.MOVIE -> Icons.Outlined.Movie
    BookingCategory.TURF -> Icons.Outlined.SportsSoccer
    BookingCategory.EVENT -> Icons.Outlined.Event
}

private fun bookingLabel(category: BookingCategory): String = when (category) {
    BookingCategory.MOVIE -> "MOVIE BOOKING"
    BookingCategory.TURF -> "TURF BOOKING"
    BookingCategory.EVENT -> "EVENT BOOKING"
}

private fun summaryItemLabel(category: BookingCategory): String = when (category) {
    BookingCategory.MOVIE -> "Tickets"
    BookingCategory.TURF -> "Slot charge"
    BookingCategory.EVENT -> "Tickets"
}

private fun bookingItemName(category: BookingCategory): String = when (category) {
    BookingCategory.MOVIE -> "Tickets"
    BookingCategory.TURF -> "Turf booking"
    BookingCategory.EVENT -> "Event booking"
}

private fun paymentMethodIcon(type: PaymentMethodType): ImageVector = when (type) {
    PaymentMethodType.UPI -> Icons.Rounded.AccountBalanceWallet
    PaymentMethodType.CARD -> Icons.Rounded.CreditCard
    PaymentMethodType.NET_BANKING -> Icons.Rounded.AccountBalance
}

private fun upiPaymentSucceeded(data: Intent?): Boolean {
    val response = data?.getStringExtra("response")
        ?: data?.getStringExtra("Response")
        ?: data?.dataString
        ?: return false
    return response.split('&').any { part ->
        val pieces = part.split('=', limit = 2)
        pieces.size == 2 && pieces[0].equals("status", ignoreCase = true) &&
            pieces[1].equals("success", ignoreCase = true)
    }
}
