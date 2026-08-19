package com.entrymyslot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.entrymyslot.ui.theme.*

// ==========================================
// 1. MOVIE DETAILS SCREEN
// ==========================================
@Composable
fun MovieDetailsScreen(onBookClick: () -> Unit = {}) {
    Scaffold(
        containerColor = SolidDarkGrey,
        bottomBar = {
            Surface(color = SurfaceGrey.copy(alpha = 0.95f), shadowElevation = 16.dp) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = onBookClick,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Text(text = "Book Tickets", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Trailer/Poster Area
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircleOutline, contentDescription = "Play", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))

                    // Top Bar Overlay
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                    }
                }
            }

            // Movie Info
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Deadpool & Wolverine", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = RatingRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "9.2/10", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(text = " (25K Votes)", fontSize = 12.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BadgeChip("2D, 3D, IMAX")
                        BadgeChip("English, Tamil")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "2h 7m", color = TextWhite, fontSize = 14.sp)
                        Text(text = "•", color = TextMuted)
                        Text(text = "Action, Comedy", color = TextWhite, fontSize = 14.sp)
                        Text(text = "•", color = TextMuted)
                        Text(text = "U/A", color = TextWhite, fontSize = 14.sp, modifier = Modifier.border(1.dp, BorderGrey, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "About the movie", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A listless Wade Wilson toils away in civilian life with his days as the morally flexible mercenary, Deadpool, behind him. But when his homeworld faces an existential threat, Wade must reluctantly suit-up again.",
                        fontSize = 13.sp, color = TextMuted, lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeChip(text: String) {
    Text(
        text = text,
        color = TextWhite,
        fontSize = 12.sp,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(SurfaceGrey).padding(horizontal = 8.dp, vertical = 4.dp)
    )
}


// ==========================================
// 2. THEATRE & SEAT BOOKING SCREEN
// ==========================================
@Composable
fun MovieSeatBookingScreen(onBackClick: () -> Unit = {}) {
    // A Set to hold selected seat IDs like "A1", "C4"
    var selectedSeats by remember { mutableStateOf(setOf<String>()) }

    val premiumPrice = 190.0
    val standardPrice = 150.0

    // Simple logic to calculate cost based on row (A, B are Premium. C, D, E are Standard)
    val totalSeatCost = selectedSeats.sumOf { seatId ->
        if (seatId.startsWith("A") || seatId.startsWith("B")) premiumPrice else standardPrice
    }

    Scaffold(
        containerColor = SolidDarkGrey,
        topBar = { SeatBookingTopBar(onBackClick) },
        bottomBar = { MovieBookingBottomBar(selectedSeats.size, totalSeatCost) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Screen Curved Line
            item {
                CurvedScreenDisplay()
            }

            // Seat Grid
            item {
                SeatLayoutGrid(selectedSeats) { seatId ->
                    val newSeats = selectedSeats.toMutableSet()
                    if (newSeats.contains(seatId)) newSeats.remove(seatId)
                    else if (newSeats.size < 10) newSeats.add(seatId) // Max 10 seats
                    selectedSeats = newSeats
                }
            }

            // Seat Legend
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SeatLegend()
            }

            // Billing Summary (Only if seats are selected)
            if (selectedSeats.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    MovieBillSummary(selectedSeats.size, totalSeatCost)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatBookingTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(text = "Deadpool & Wolverine - 3D", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "INOX: Reliance Mall, Erode | Today, 07:15 PM", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                // Cancellation Badge BookMyShow Style
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = GreenSuccess, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Cancellation Available", color = GreenSuccess, fontSize = 10.sp)
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SolidDarkGrey)
    )
}

@Composable
fun CurvedScreenDisplay() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.fillMaxWidth(0.8f).height(40.dp)) {
            val path = Path().apply {
                moveTo(0f, size.height)
                quadraticBezierTo(size.width / 2, 0f, size.width, size.height)
            }
            drawPath(
                path = path,
                color = PrimaryOrange.copy(alpha = 0.5f),
                style = Stroke(width = 6f)
            )
        }
        Text(text = "All eyes this way", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
    }
}

@Composable
fun SeatLayoutGrid(selectedSeats: Set<String>, onSeatClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

        // PREMIUM SEATS
        Text(text = "PREMIUM - ₹190", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))
        val premiumRows = listOf("A", "B")
        premiumRows.forEach { row ->
            SeatRow(row = row, seatCount = 8, selectedSeats = selectedSeats, onSeatClick = onSeatClick)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // STANDARD SEATS
        Text(text = "STANDARD - ₹150", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))
        val standardRows = listOf("C", "D", "E", "F")
        standardRows.forEach { row ->
            SeatRow(row = row, seatCount = 8, selectedSeats = selectedSeats, onSeatClick = onSeatClick)
        }
    }
}

@Composable
fun SeatRow(row: String, seatCount: Int, selectedSeats: Set<String>, onSeatClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = row, color = TextMuted, fontSize = 12.sp, modifier = Modifier.width(20.dp))
        Spacer(modifier = Modifier.width(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..seatCount) {
                // Gap in the middle for aisle
                if (i == (seatCount / 2) + 1) Spacer(modifier = Modifier.width(16.dp))

                val seatId = "$row$i"
                val isSelected = selectedSeats.contains(seatId)
                // Randomly mock some seats as sold (e.g., C3, C4, D1)
                val isSold = seatId in listOf("C3", "C4", "D1", "A5", "A6")

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = !isSold) { onSeatClick(seatId) }
                        .background(if (isSold) SurfaceGrey else if (isSelected) PrimaryOrange else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSold) Color.Transparent else if (isSelected) PrimaryOrange else BorderGrey,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isSold) {
                        Text(text = "$i", color = if (isSelected) Color.White else TextWhite, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SeatLegend() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        LegendItem(text = "Available", bgColor = Color.Transparent, borderColor = BorderGrey, textColor = TextWhite)
        LegendItem(text = "Selected", bgColor = PrimaryOrange, borderColor = PrimaryOrange, textColor = Color.White)
        LegendItem(text = "Sold", bgColor = SurfaceGrey, borderColor = Color.Transparent, textColor = TextMuted)
    }
}

@Composable
fun MovieBillSummary(slotsCount: Int, totalSeatCost: Double) {
    val platformFee = 30.0 // Standard movie platform fee
    val cgst = platformFee * 0.09
    val sgst = platformFee * 0.09
    val total = totalSeatCost + platformFee + cgst + sgst

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Booking Summary", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceGrey).padding(16.dp)
        ) {
            BillRow("Seat Cost ($slotsCount Ticket${if(slotsCount > 1) "s" else ""})", "₹${String.format(Locale.US, "%.2f", totalSeatCost)}")
            Spacer(modifier = Modifier.height(8.dp))
            BillRow("Platform Fee", "₹${String.format(Locale.US, "%.2f", platformFee)}")

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "GST on Platform Fee", color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "CGST (9%)", color = TextMuted, fontSize = 12.sp)
                Text(text = "₹${String.format(Locale.US, "%.2f", cgst)}", color = TextMuted, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "SGST (9%)", color = TextMuted, fontSize = 12.sp)
                Text(text = "₹${String.format(Locale.US, "%.2f", sgst)}", color = TextMuted, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderGrey, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total to pay", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "₹${String.format(Locale.US, "%.2f", total)}", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun MovieBookingBottomBar(slotsCount: Int, totalCost: Double) {
    Surface(
        color = SurfaceGrey.copy(alpha = 0.95f),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (slotsCount == 0) {
                Text(text = "Select your seats", color = TextMuted, fontSize = 14.sp)
            } else {
                Column {
                    Text(text = "$slotsCount Ticket(s) Selected", fontSize = 11.sp, color = TextMuted)
                    // Showing a rough final total (just Seat + Platform fee roughly) before detailed breakdown
                    val roughTotal = totalCost + 30.0 + (30.0 * 0.18)
                    Text(text = "₹${String.format(Locale.US, "%.2f", roughTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }

            Button(
                onClick = { /* Proceed */ },
                enabled = slotsCount > 0,
                modifier = Modifier.width(150.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (slotsCount > 0) PrimaryOrange else BorderGrey)
            ) {
                Text(text = "Pay Now", fontSize = 16.sp, color = if (slotsCount > 0) Color.White else TextMuted, fontWeight = FontWeight.Bold)
            }
        }
    }
}