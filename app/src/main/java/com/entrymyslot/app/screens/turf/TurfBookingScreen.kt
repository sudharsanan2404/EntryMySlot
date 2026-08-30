package com.entrymyslot.app.screens.turf

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import java.text.SimpleDateFormat
import java.util.*

private val TurfBlueTop = Color(0xFF063DB5)
private val TurfBlueBottom = Color(0xFF041F5D)

private val TurfOrange = Color(0xFFFF8A00)
private val TurfWhite = Color.White
private val TurfGray = Color(0xFFB8C0D0)

private val TurfCard = Color(0xFF111D32)
private val TurfCardLight = Color(0xFF142B58)

private val AvailableColor = Color(0xFF163D63)
private val BookedColor = Color(0xFF242A35)
private val SelectedColor = Color(0xFFFF8A00)

data class TurfSlotItem(
    val id: Int,
    val time: String,
    val booked: Boolean
)

@Composable
fun TurfBookingScreen(
    resourceId: String,
    onBackClick: () -> Unit = {},
    onBookingSuccess: () -> Unit = {}
) {
    val viewModel = remember { TurfBookingViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val resourceIdLong = resourceId.toLongOrNull()

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedSlots by remember { mutableStateOf(setOf<Int>()) }

    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

    if (resourceIdLong != null) {
        LaunchedEffect(resourceIdLong, dateStr) {
            viewModel.loadAvailability(resourceIdLong, dateStr)
        }
    }

    val slots = remember(uiState.availability, dateStr) {
        val slotMap = uiState.availability?.slots?.find { it.date == dateStr }?.slots
            ?: emptyList()
        slotMap.mapIndexed { idx, ts ->
            val timeLabel = "${ts.startTime} - ${ts.endTime}"
            val booked = ts.availableUnits <= 0
            TurfSlotItem(id = idx, time = timeLabel, booked = booked)
        }
    }

    val pricePerSlot = uiState.availability?.slots?.find { it.date == dateStr }?.slots?.firstOrNull()?.price?.toDoubleOrNull()?.toInt()
        ?: 800

    val totalPrice = selectedSlots.size * pricePerSlot

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TurfBlueTop,
                        Color(0xFF0737A4),
                        Color(0xFF062E88),
                        TurfBlueBottom
                    )
                )
            )
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TurfWhite,
                            modifier = Modifier.size(28.dp).clickable { onBackClick() }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Book Turf", color = TurfWhite, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    SectionTitle(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Select Date"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DateSelector(
                        selectedDate = selectedDate,
                        onDateSelected = {
                            selectedDate = it
                            selectedSlots = emptySet()
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(
                            icon = Icons.Outlined.Schedule,
                            title = "Select Time Slot"
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${slots.size} Slots",
                            color = TurfGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    SlotLegend()
                    Spacer(modifier = Modifier.height(18.dp))
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        slots.chunked(3).forEach { rowSlots ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowSlots.forEach { slot ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        TurfSlotItem(
                                            slot = slot,
                                            selected = selectedSlots.contains(slot.id),
                                            onClick = {
                                                if (!slot.booked) {
                                                    selectedSlots =
                                                        if (selectedSlots.contains(slot.id)) {
                                                            selectedSlots - slot.id
                                                        } else {
                                                            selectedSlots + slot.id
                                                        }
                                                }
                                            }
                                        )
                                    }
                                }
                                repeat(3 - rowSlots.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Every booking comes with 55 minutes of active playtime",
                        color = TurfGray.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    if (selectedSlots.isNotEmpty()) {
                        BookingSummary(
                            selectedSlots = selectedSlots.size,
                            totalPrice = totalPrice,
                            pricePerHour = pricePerSlot
                        )
                    }
                }
            }

            BottomBookingBar(
                selectedSlots = selectedSlots.size,
                totalPrice = totalPrice,
                onContinueClick = onBookingSuccess
            )
        }
    }
}


@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = TurfOrange, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = title, color = TurfWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DateSelector(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        for (i in 0..6) {
            val date = Calendar.getInstance()
            date.add(Calendar.DAY_OF_YEAR, i)
            val selected = date.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                    date.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
            DateItem(date = date, selected = selected, onClick = { onDateSelected(date) })
        }
    }
}

@Composable
private fun DateItem(date: Calendar, selected: Boolean, onClick: () -> Unit) {
    val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(date.time)
    val dayNumber = date.get(Calendar.DAY_OF_MONTH)
    val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(date.time)

    Column(
        modifier = Modifier
            .width(43.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) TurfOrange else TurfCardLight)
            .border(
                width = 1.dp,
                color = if (selected) TurfOrange else Color(0xFF31528A),
                shape = RoundedCornerShape(11.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = dayName, color = if (selected) Color.White else TurfGray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = dayNumber.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = monthName, color = if (selected) Color.White.copy(alpha = 0.85f) else TurfGray, fontSize = 9.sp)
    }
}

@Composable
private fun SlotLegend() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(color = AvailableColor, text = "Available")
        LegendItem(color = SelectedColor, text = "Selected")
        LegendItem(color = BookedColor, text = "Booked")
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text, color = TurfGray, fontSize = 11.sp)
    }
}

@Composable
private fun TurfSlotItem(
    slot: TurfSlotItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        slot.booked -> BookedColor
        selected -> SelectedColor
        else -> AvailableColor
    }
    val borderColor = when {
        slot.booked -> Color(0xFF242A35)
        selected -> Color(0xFFFF8A00)
        else -> Color(0xFF1C5380)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .clickable(enabled = !slot.booked, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
            contentDescription = null,
            tint = if (slot.booked) Color(0xFF535B69) else Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = slot.time, color = if (slot.booked) Color(0xFF8B95A1) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (slot.booked) "Booked" else if (selected) "Selected" else "Available",
                color = if (slot.booked) Color(0xFF8B95A1).copy(alpha = 0.7f) else if (selected) Color.White.copy(alpha = 0.9f) else Color(0xFF9FC7EA),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BookingSummary(selectedSlots: Int, totalPrice: Int, pricePerHour: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TurfCard)
            .border(width = 1.dp, color = Color(0xFF2A426B), shape = RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(text = "Booking Summary", color = TurfWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Selected Slots", color = TurfGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "$selectedSlots slot(s)", color = TurfWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(7.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Price / Slot", color = TurfGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "₹$pricePerHour", color = TurfWhite, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF293A59)))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Total", color = TurfWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "₹$totalPrice", color = TurfOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BottomBookingBar(selectedSlots: Int, totalPrice: Int, onContinueClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF061F58))
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = if (selectedSlots == 0) "Select a slot" else "$selectedSlots slot(s) selected", color = TurfGray, fontSize = 11.sp)
            Text(text = "₹$totalPrice", color = TurfWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(if (selectedSlots > 0) TurfOrange else Color(0xFF4A5261))
                .clickable(enabled = selectedSlots > 0, onClick = onContinueClick)
                .padding(horizontal = 25.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Continue", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}