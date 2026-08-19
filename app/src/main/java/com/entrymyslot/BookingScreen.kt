package com.entrymyslot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.entrymyslot.ui.theme.*

@Composable
fun BookingScreen(onBackClick: () -> Unit = {}) {
    // States for interaction
    var selectedDateIndex by remember { mutableStateOf(0) }
    var selectedSlots by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        containerColor = SolidDarkGrey,
        topBar = { BookingTopBar(onBackClick) },
        bottomBar = { BookingBottomBar(selectedSlots.size) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Date Selection
            item {
                Spacer(modifier = Modifier.height(16.dp))
                DateSelectionSection(selectedDateIndex) { selectedDateIndex = it }
            }

            // 2. Slot Legend & Info
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SlotLegendAndInfo()
            }

            // 3. 24-Hour Slots Grid
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SlotsGridSection(selectedSlots) { slotIndex ->
                    val newSlots = selectedSlots.toMutableSet()
                    if (newSlots.contains(slotIndex)) newSlots.remove(slotIndex)
                    else newSlots.add(slotIndex)
                    selectedSlots = newSlots
                }
            }

            // 4. Offers & Coupons
            item {
                Spacer(modifier = Modifier.height(32.dp))
                CouponSection()
            }

            // 5. Bill Summary (Shows only if at least 1 slot is selected)
            if (selectedSlots.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    BillSummarySection(selectedSlots.size)
                }
            }

            // 6. Cancellation Policy (Transparent & Clear)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                CancellationPolicySection()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(text = "Kickoff Arena", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Football • Synthetic", color = TextMuted, fontSize = 12.sp)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SolidDarkGrey)
    )
}

@Composable
fun DateSelectionSection(selectedIndex: Int, onDateSelected: (Int) -> Unit) {
    Column {
        Text(
            text = "Select Date",
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Mocking dates starting from current context date (Aug 17)
        val dates = listOf("17 Aug" to "Mon", "18 Aug" to "Tue", "19 Aug" to "Wed", "20 Aug" to "Thu", "21 Aug" to "Fri", "22 Aug" to "Sat")

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(dates) { index, datePair ->
                val isSelected = selectedIndex == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDateSelected(index) }
                        .background(if (isSelected) PrimaryOrange else SurfaceGrey)
                        .border(1.dp, if (isSelected) PrimaryOrange else BorderGrey, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(text = datePair.second, fontSize = 12.sp, color = if (isSelected) Color.White else TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = datePair.first.split(" ")[0], fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else TextWhite)
                    Text(text = datePair.first.split(" ")[1], fontSize = 10.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted)
                }
            }
        }
    }
}

@Composable
fun SlotLegendAndInfo() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Explicit 55 mins info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E3A8A).copy(alpha = 0.3f))
                .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Note: 1 Slot = 55 minutes of playtime", color = Color(0xFF60A5FA), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(text = "Available", bgColor = Color.Transparent, borderColor = BorderGrey, textColor = TextWhite)
            LegendItem(text = "Selected", bgColor = PrimaryOrange, borderColor = PrimaryOrange, textColor = Color.White)
            LegendItem(text = "Sold", bgColor = SurfaceGrey, borderColor = Color.Transparent, textColor = TextMuted)
        }
    }
}

@Composable
fun LegendItem(text: String, bgColor: Color, borderColor: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.dp, borderColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = textColor, fontSize = 12.sp)
    }
}

@Composable
fun SlotsGridSection(selectedSlots: Set<Int>, onSlotClick: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Available Slots", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // 24 Slots Generation (4 columns x 6 rows for perfect mobile fit)
        val slots = (0..23).map { String.format(Locale.US, "%02d:00", it) }

        slots.chunked(4).forEachIndexed { rowIndex, rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowSlots.forEachIndexed { colIndex, time ->
                    val actualIndex = (rowIndex * 4) + colIndex
                    // Mocking some slots as sold out (e.g., 07:00, 18:00, 19:00)
                    val isSold = actualIndex == 7 || actualIndex == 18 || actualIndex == 19
                    val isSelected = selectedSlots.contains(actualIndex)

                    val bgColor = when {
                        isSold -> SurfaceGrey
                        isSelected -> PrimaryOrange
                        else -> Color.Transparent
                    }
                    val borderColor = when {
                        isSold -> Color.Transparent
                        isSelected -> PrimaryOrange
                        else -> BorderGrey
                    }
                    val textColor = when {
                        isSold -> TextMuted
                        isSelected -> Color.White
                        else -> TextWhite
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isSold) { onSlotClick(actualIndex) }
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = time, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CouponSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGrey)
                .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                .clickable { /* Open Coupons Sheet */ }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Discount, contentDescription = "Coupon", tint = PrimaryOrange)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Apply Coupon / Offers", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "View available discounts", color = TextMuted, fontSize = 12.sp)
                }
            }
            Text(text = "Apply", color = PrimaryOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BillSummarySection(slotsCount: Int) {
    val slotCost = 1200.0 * slotsCount
    val platformFee = 50.0
    val cgst = platformFee * 0.09
    val sgst = platformFee * 0.09
    val discount = if (slotsCount >= 2) 200.0 else 0.0 // Dummy discount for 2+ slots
    val total = slotCost + platformFee + cgst + sgst - discount

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Bill Summary", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGrey)
                .padding(16.dp)
        ) {
            BillRow("Slot Cost ($slotsCount x ₹1200)", "₹${String.format(Locale.US, "%.2f", slotCost)}")
            Spacer(modifier = Modifier.height(8.dp))
            BillRow("Platform Fee", "₹${String.format(Locale.US, "%.2f", platformFee)}")

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "GST on Platform Fee", color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))

            // Indented GST Rows
            Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "CGST (9%)", color = TextMuted, fontSize = 12.sp)
                Text(text = "₹${String.format(Locale.US, "%.2f", cgst)}", color = TextMuted, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "SGST (9%)", color = TextMuted, fontSize = 12.sp)
                Text(text = "₹${String.format(Locale.US, "%.2f", sgst)}", color = TextMuted, fontSize = 12.sp)
            }

            if (discount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                BillRow("Discounts", "-₹${String.format(Locale.US, "%.2f", discount)}", color = TextGreen)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderGrey, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Total Pay
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total to pay", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "₹${String.format(Locale.US, "%.2f", total)}", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun BillRow(title: String, amount: String, color: Color = TextWhite) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = TextMuted, fontSize = 13.sp)
        Text(text = amount, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CancellationPolicySection() {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarningRed.copy(alpha = 0.1f))
            .border(1.dp, WarningRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = "Policy", tint = WarningRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cancellation Policy", color = WarningRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        PolicyPoint("Before 10 hours: Full cancellation allowed.")
        Spacer(modifier = Modifier.height(6.dp))
        PolicyPoint("Less than 10 hours: Only 1 time slot change permitted. No cancellations or further changes allowed.")
    }
}

@Composable
fun PolicyPoint(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = "•", color = WarningRed, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
        Text(text = text, color = WarningRed.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
fun BookingBottomBar(slotsCount: Int) {
    Surface(
        color = SurfaceGrey.copy(alpha = 0.95f),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (slotsCount == 0) {
                Text(text = "Please select a slot", color = TextMuted, fontSize = 14.sp)
            } else {
                Column {
                    Text(text = "$slotsCount Slot(s) Selected", fontSize = 11.sp, color = TextMuted)
                    Text(text = "Proceed to pay", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }

            Button(
                onClick = { /* TODO: Payment Gateway */ },
                enabled = slotsCount > 0,
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = BorderGrey
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (slotsCount > 0) Brush.horizontalGradient(listOf(PrimaryOrange, PrimaryOrangeEnd))
                            else Brush.horizontalGradient(listOf(BorderGrey, BorderGrey)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pay Now",
                        fontSize = 16.sp,
                        color = if (slotsCount > 0) Color.White else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}