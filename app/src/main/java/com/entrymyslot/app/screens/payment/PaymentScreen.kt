package com.entrymyslot.app.screens.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.GlowBackground

private val PaymentOrange = Color(0xFFFF8A3D)
private val PaymentWhite = Color.White
private val PaymentGray = Color(0xFF98A2B3)
private val PaymentCard = Color(0xFF0E1739).copy(alpha = .94f)
private val PaymentCardLight = Color(0xFF1B2854)
private val PaymentBorder = Color(0xFF31426F).copy(alpha = .72f)
private val PaymentDivider = Color.White.copy(alpha = .09f)
private val PaymentGreen = Color(0xFF35C66B)

enum class BookingCategory { MOVIE, TURF, EVENT }

data class BookingDetails(
    val title: String,
    val category: BookingCategory,
    val date: String,
    val time: String,
    val location: String,
    val details: String,
    val imageUrl: String? = null
)

data class PaymentMethod(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    bookingDetails: BookingDetails,
    onBackClick: () -> Unit = {},
    onPaySuccess: () -> Unit = {}
) {
    var selectedMethodId by remember { mutableStateOf("upi") }

    val ticketPrice = 360
    val convenienceFee = 30
    val taxes = 18
    val totalAmount = ticketPrice + convenienceFee + taxes
    val methods = listOf(
        PaymentMethod("upi", "UPI (GPay, PhonePe, Paytm)", Icons.Rounded.AccountBalanceWallet, "Pay instantly using any UPI app", "FASTEST"),
        PaymentMethod("card", "Credit / Debit Card", Icons.Rounded.CreditCard, "Visa, Mastercard, RuPay"),
        PaymentMethod("netbanking", "Netbanking", Icons.Rounded.AccountBalance, "All major Indian banks supported")
    )

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
                    PaymentOptionsSection(methods, selectedMethodId) { selectedMethodId = it }
                }
                item {
                    OrderSummaryCard(
                        bookingDetails = bookingDetails,
                        ticketPrice = ticketPrice,
                        convenienceFee = convenienceFee,
                        taxes = taxes,
                        totalAmount = totalAmount,
                        enabled = selectedMethodId.isNotBlank(),
                        onPayClick = onPaySuccess
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
    Surface(onClick = onClick, color = if (isSelected) PaymentOrange.copy(alpha = .085f) else Color.Transparent) {
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
                        Surface(color = PaymentGreen.copy(alpha = .15f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                badge,
                                color = PaymentGreen,
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
                method.icon,
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
    totalAmount: Int,
    enabled: Boolean,
    onPayClick: () -> Unit
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
            Spacer(modifier = Modifier.height(17.dp))
            Button(
                onClick = onPayClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaymentOrange,
                    contentColor = PaymentWhite,
                    disabledContainerColor = PaymentOrange.copy(alpha = .35f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Proceed to pay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
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
        Icon(Icons.Rounded.Lock, null, tint = PaymentGreen, modifier = Modifier.size(14.dp))
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
