package com.entrymyslot.app.screens.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.R

// ------------------------------------------------------------
// COLORS
// ------------------------------------------------------------
private val PaymentBlueTop = Color(0xFF063DB5)
private val PaymentBlueBottom = Color(0xFF041F5D)
private val PaymentOrange = Color(0xFFFF8A00)
private val PaymentWhite = Color.White
private val PaymentGray = Color(0xFFB8C0D0)
private val PaymentCard = Color(0xFF111D32)
private val PaymentCardLight = Color(0xFF142B58)
private val PaymentSuccessGreen = Color(0xFF4CAF50)

// ------------------------------------------------------------
// MODELS
// ------------------------------------------------------------
enum class BookingCategory { MOVIE, TURF, EVENT }

data class BookingDetails(
    val title: String,
    val category: BookingCategory,
    val date: String,
    val time: String,
    val location: String,
    val details: String, // e.g., "Seats: A3, A4" or "Slots: 6 PM - 7 PM"
    val imageUrl: String? = null
)

data class PaymentMethod(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String
)

// ------------------------------------------------------------
// PAYMENT SCREEN
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    bookingDetails: BookingDetails,
    onBackClick: () -> Unit = {},
    onPaySuccess: () -> Unit = {}
) {
    var selectedMethodId by remember { mutableStateOf("upi") }
    var isCouponApplied by remember { mutableStateOf(false) }

    val ticketPrice = 360
    val convenienceFee = 30
    val taxes = 18
    val discount = if (isCouponApplied) 50 else 0
    val totalAmount = (ticketPrice + convenienceFee + taxes) - discount

    val paymentMethods = listOf(
        PaymentMethod("upi", "UPI", Icons.Outlined.AccountBalanceWallet, "Pay using UPI"),
        PaymentMethod("card", "Credit / Debit Card", Icons.Outlined.CreditCard, "Visa, Mastercard, RuPay"),
        PaymentMethod("netbanking", "Net Banking", Icons.Outlined.AccountBalance, "All major banks available"),
        PaymentMethod("wallets", "Wallets", Icons.Outlined.Wallet, "Amazon Pay, PhonePe & more")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment", color = PaymentWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = PaymentWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomPaymentBar(totalAmount, selectedMethodId.isNotEmpty()) {
                onPaySuccess()
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PaymentBlueTop, PaymentBlueBottom)))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Booking Summary Card
                item { BookingSummaryCard(bookingDetails) }

                // 2. Coupon Section
                item { CouponSection(isCouponApplied) { isCouponApplied = !isCouponApplied } }

                // 3. Price Details
                item { PriceDetailsSection(ticketPrice, convenienceFee, taxes, discount, totalAmount) }

                // 4. Payment Methods
                item {
                    PaymentMethodsSection(
                        methods = paymentMethods,
                        selectedId = selectedMethodId,
                        onSelect = { selectedMethodId = it }
                    )
                }

                // 5. Security Note
                item { SecurityNote() }
                
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun BookingSummaryCard(details: BookingDetails) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaymentCard),
        border = BorderStroke(1.dp, Color(0xFF2A426B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaymentCardLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(details.category) {
                        BookingCategory.MOVIE -> Icons.Outlined.Movie
                        BookingCategory.TURF -> Icons.Outlined.SportsSoccer
                        BookingCategory.EVENT -> Icons.Outlined.Event
                    },
                    contentDescription = null,
                    tint = PaymentGray,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = details.title,
                    color = PaymentWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = details.location,
                    color = PaymentGray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = PaymentOrange, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${details.date} • ${details.time}",
                        color = PaymentWhite.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = details.details,
                    color = PaymentOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CouponSection(isApplied: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PaymentCard),
        border = BorderStroke(1.dp, if (isApplied) PaymentOrange.copy(alpha = 0.5f) else Color(0xFF2A426B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocalOffer, null, tint = PaymentOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isApplied) "FIRSTBOOK applied!" else "Have a coupon?",
                        color = PaymentWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (isApplied) {
                        Text("You saved ₹50", color = PaymentSuccessGreen, fontSize = 11.sp)
                    }
                }
            }
            
            Text(
                text = if (isApplied) "Remove" else "Apply Coupon →",
                color = PaymentOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onToggle() }
            )
        }
    }
}

@Composable
private fun PriceDetailsSection(
    ticket: Int,
    fee: Int,
    taxes: Int,
    discount: Int,
    total: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Price Details",
            color = PaymentWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaymentCard),
            border = BorderStroke(1.dp, Color(0xFF2A426B))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                PriceRow("Ticket Price", "₹$ticket")
                PriceRow("Convenience Fee", "₹$fee")
                PriceRow("Taxes", "₹$taxes")
                
                if (discount > 0) {
                    PriceRow("Discount", "- ₹$discount", isDiscount = true)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF293A59)))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Amount", color = PaymentWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("₹$total", color = PaymentWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PaymentGray, fontSize = 14.sp)
        Text(
            text = value,
            color = if (isDiscount) PaymentSuccessGreen else PaymentWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentMethodsSection(
    methods: List<PaymentMethod>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Payment Method",
            color = PaymentWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            methods.forEach { method ->
                val isSelected = selectedId == method.id
                PaymentMethodCard(method, isSelected) { onSelect(method.id) }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF0038A8).copy(alpha = 0.3f) else PaymentCard,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected) PaymentOrange else Color(0xFF2A426B)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PaymentOrange.copy(alpha = 0.1f) else PaymentCardLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = method.icon,
                    contentDescription = null,
                    tint = if (isSelected) PaymentOrange else PaymentGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(method.name, color = PaymentWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(method.description, color = PaymentGray, fontSize = 12.sp)
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = PaymentOrange,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(1.dp, PaymentGray.copy(alpha = 0.5f), RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
private fun SecurityNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Lock, null, tint = PaymentGray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Secure payment", color = PaymentWhite.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Your payment information is protected", color = PaymentGray, fontSize = 10.sp)
        }
    }
}

@Composable
private fun BottomPaymentBar(amount: Int, enabled: Boolean, onPayClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF061F58),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total Amount", color = PaymentGray, fontSize = 12.sp)
                Text("₹$amount", color = PaymentWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }

            Button(
                onClick = onPayClick,
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaymentOrange,
                    disabledContainerColor = Color(0xFF4A5261)
                ),
                modifier = Modifier
                    .height(54.dp)
                    .fillMaxWidth(0.6f),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Pay ₹$amount",
                    color = PaymentWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
