package com.entrymyslot.app.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val MovieBlueTop = Color(0xFF063DB5)
private val MovieBlueBottom = Color(0xFF041F5D)
private val MovieOrange = Color(0xFFFF8A00)
private val MovieWhite = Color.White
private val MovieGray = Color(0xFFB8C0D0)
private val MovieCard = Color(0xFF111D32)
private val MovieCardLight = Color(0xFF142B58)
private val SeatAvailable = Color(0xFF183E65)
private val SeatSelected = Color(0xFFFF8A00)
private val SeatBooked = Color(0xFF3A404B)

@Composable
fun MovieBookingScreen(
    cinema: Cinema,
    initialTime: String,
    selectedDate: Calendar,
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    var selectedTime by remember { mutableStateOf(initialTime) }
    var seatCountToBook by remember { mutableIntStateOf(1) }
    var selectedSeats by remember { mutableStateOf(setOf<String>()) }

    val ticketPrice = 180
    val totalPrice = selectedSeats.size * ticketPrice

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MovieBlueTop, MovieBlueBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MovieWhite,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Select Seats", color = MovieWhite, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Selected Cinema info
                item {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                        Text(cinema.name, color = MovieWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(cinema.location, color = MovieGray, fontSize = 13.sp)
                    }
                }

                // Show Times in horizontal
                item {
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text("Show Times", color = MovieWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 18.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(cinema.showTimes) { time ->
                                val isSelected = time == selectedTime
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) MovieOrange else MovieCardLight)
                                        .clickable { 
                                            selectedTime = time
                                            selectedSeats = emptySet()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(time, color = MovieWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Seat Count Selector
                item {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                        Text("How many seats?", color = MovieWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(8) { i ->
                                val count = i + 1
                                val isSelected = count == seatCountToBook
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MovieOrange else MovieCardLight)
                                        .clickable { 
                                            seatCountToBook = count
                                            selectedSeats = emptySet()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(count.toString(), color = MovieWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Screen & Seats
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    SeatLegend()
                    Spacer(modifier = Modifier.height(30.dp))
                    CinemaScreen()
                    Spacer(modifier = Modifier.height(30.dp))
                    SeatLayout(
                        selectedSeats = selectedSeats,
                        countToBook = seatCountToBook,
                        onSeatClick = { newSelection ->
                            selectedSeats = newSelection
                        }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                }

                // Summary
                item {
                    if (selectedSeats.isNotEmpty()) {
                        MovieBookingSummary(selectedSeats.size, ticketPrice, totalPrice)
                    }
                }
            }

            MovieBottomBar(selectedSeats.size, totalPrice, onContinueClick)
        }
    }
}

@Composable
private fun SeatLegend() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        LegendItem(SeatAvailable, "Available")
        Spacer(modifier = Modifier.width(20.dp))
        LegendItem(SeatSelected, "Selected")
        Spacer(modifier = Modifier.width(20.dp))
        LegendItem(SeatBooked, "Booked")
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = MovieGray, fontSize = 12.sp)
    }
}

@Composable
private fun CinemaScreen() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth(0.7f).height(4.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.8f)))
        Spacer(modifier = Modifier.height(8.dp))
        Text("SCREEN", color = MovieGray, fontSize = 10.sp, letterSpacing = 4.sp)
    }
}

@Composable
private fun SeatLayout(
    selectedSeats: Set<String>,
    countToBook: Int,
    onSeatClick: (Set<String>) -> Unit
) {
    val rows = listOf("A", "B", "C", "D", "E", "F", "G")
    val bookedSeats = setOf("A2", "B4", "C1", "D3", "E2", "F4", "A6", "B7", "C5", "E8")

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(row, color = MovieGray, fontSize = 12.sp, modifier = Modifier.width(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (seatNum in 1..8) {
                        val seatId = "$row$seatNum"
                        val isBooked = seatId in bookedSeats
                        val isSelected = seatId in selectedSeats

                        SeatItem(
                            isSelected = isSelected,
                            isBooked = isBooked,
                            onClick = {
                                if (!isBooked) {
                                    // Sequential Selection Logic
                                    val newSelection = mutableSetOf<String>()
                                    var possible = true
                                    for (i in 0 until countToBook) {
                                        val nextSeatNum = seatNum + i
                                        if (nextSeatNum > 8) {
                                            possible = false
                                            break
                                        }
                                        val nextSeatId = "$row$nextSeatNum"
                                        if (nextSeatId in bookedSeats) {
                                            possible = false
                                            break
                                        }
                                        newSelection.add(nextSeatId)
                                    }
                                    if (possible) {
                                        onSeatClick(newSelection)
                                    }
                                }
                            }
                        )
                        if (seatNum == 2 || seatNum == 6) Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatItem(isSelected: Boolean, isBooked: Boolean, onClick: () -> Unit) {
    val color = when {
        isBooked -> SeatBooked
        isSelected -> SeatSelected
        else -> SeatAvailable
    }
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 26.dp)
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .clickable(enabled = !isBooked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun MovieBookingSummary(count: Int, price: Int, total: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MovieCard)
            .border(1.dp, Color(0xFF2A426B), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text("Booking Summary", color = MovieWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        SummaryRow("Tickets", "$count seat(s)")
        SummaryRow("Price", "₹$price × $count")
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF293A59)))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", color = MovieWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("₹$total", color = MovieOrange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MovieGray, fontSize = 14.sp)
        Text(value, color = MovieWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MovieBottomBar(count: Int, total: Int, onContinueClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF061F58))
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(if (count == 0) "Select seats" else "$count seat(s) selected", color = MovieGray, fontSize = 12.sp)
            Text("₹$total", color = MovieWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onContinueClick,
            enabled = count > 0,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MovieOrange),
            modifier = Modifier.height(50.dp).padding(start = 16.dp)
        ) {
            Text("Continue", color = MovieWhite, fontWeight = FontWeight.Bold)
        }
    }
}
