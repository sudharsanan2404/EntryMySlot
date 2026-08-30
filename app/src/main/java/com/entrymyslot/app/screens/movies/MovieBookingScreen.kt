package com.entrymyslot.app.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.entrymyslot.app.data.model.SeatDto
import java.util.*

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
    showtimeId: String,
    onBackClick: () -> Unit = {},
    onBookingSuccess: () -> Unit = {}
) {
    val viewModel = remember { MovieBookingViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val showtimeIdLong = showtimeId.toLongOrNull()

    var selectedSeats by remember { mutableStateOf(setOf<String>()) }
    var seatCountToBook by remember { mutableIntStateOf(1) }

    if (uiState.seatData == null && !uiState.isLoading && uiState.error == null && showtimeIdLong != null) {
        viewModel.loadSeats(showtimeIdLong)
    }

    val ticketPrice = uiState.seatData?.seats?.firstOrNull()?.let {
        try { (it.priceMultiplier * (uiState.basePrice.toDoubleOrNull() ?: 180.0)).toInt() } catch (e: Exception) { 180 }
    } ?: 180

    val totalPrice = selectedSeats.size * ticketPrice

    val cinemaName = uiState.seatData?.cinemaName ?: "Cinema"
    val showTime = uiState.seatData?.showTime ?: ""

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
                    modifier = Modifier.size(28.dp).clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Select Seats", color = MovieWhite, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }

            if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(uiState.error!!, color = Color(0xFFFF5252), fontSize = 13.sp)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                        Text(cinemaName, color = MovieWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        if (showTime.isNotBlank()) {
                            Text("$showTime", color = MovieGray, fontSize = 13.sp)
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator(color = MovieOrange)
                        }
                    }
                } else {
                    val allSeats = uiState.seatData?.seats ?: emptyList()
                    val rows = allSeats.groupBy { it.row }.toSortedMap()

                    item {
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Text("How many seats?", color = MovieWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 18.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 18.dp)) {
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

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        SeatLegend()
                        Spacer(modifier = Modifier.height(30.dp))
                        CinemaScreen()
                        Spacer(modifier = Modifier.height(30.dp))
                    }

                    rows.forEach { (row, seatsInRow) ->
                        item {
                            SeatRow(
                                row = row,
                                seats = seatsInRow,
                                selectedSeats = selectedSeats,
                                countToBook = seatCountToBook,
                                onSeatClick = { newSelection -> selectedSeats = newSelection }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (selectedSeats.isNotEmpty()) {
                        item {
                            MovieBookingSummary(selectedSeats.size, ticketPrice, totalPrice)
                        }
                    }
                }
            }

            MovieBottomBar(selectedSeats.size, totalPrice, onContinueClick = onBookingSuccess)
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
private fun SeatRow(
    row: String,
    seats: List<SeatDto>,
    selectedSeats: Set<String>,
    countToBook: Int,
    onSeatClick: (Set<String>) -> Unit
) {
    val bookedIds = seats.filter { it.status.equals("booked", true) || it.status.equals("sold", true) }.map { it.id }.toSet()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(row, color = MovieGray, fontSize = 12.sp, modifier = Modifier.width(20.dp))
        Spacer(modifier = Modifier.width(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            seats.forEach { seat ->
                val seatId = seat.id
                val isBooked = seatId in bookedIds
                val isSelected = seatId in selectedSeats

                SeatItem(
                    isSelected = isSelected,
                    isBooked = isBooked,
                    onClick = {
                        if (!isBooked) {
                            val seatIndex = seats.indexOf(seat)
                            val newSelection = mutableSetOf<String>()
                            var possible = true
                            for (i in 0 until countToBook) {
                                val nextIdx = seatIndex + i
                                if (nextIdx >= seats.size) {
                                    possible = false
                                    break
                                }
                                val nextSeat = seats[nextIdx]
                                if (nextSeat.id in bookedIds) {
                                    possible = false
                                    break
                                }
                                newSelection.add(nextSeat.id)
                            }
                            if (possible) {
                                onSeatClick(newSelection)
                            }
                        }
                    }
                )
                if (seats.indexOf(seat) == 2 || seats.indexOf(seat) == 6) Spacer(modifier = Modifier.width(12.dp))
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
        SummaryRow("Price", "$price x $count")
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF293A59)))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", color = MovieWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("$total", color = MovieOrange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            Text("$total", color = MovieWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
