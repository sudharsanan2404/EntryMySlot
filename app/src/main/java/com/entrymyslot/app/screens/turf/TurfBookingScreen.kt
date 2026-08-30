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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.core.components.TermsAndPolicyBottomSheet


// ------------------------------------------------------------
// COLORS
// ------------------------------------------------------------

private val TurfBlueTop = Color(0xFF0B3A82)
private val TurfBlueBottom = Color(0xFF061A33)

private val TurfOrange = Color(0xFFFF8A3D)
private val TurfWhite = Color.White
private val TurfGray = Color(0xFF98A2B3)

private val TurfCardLight = Color(0xFF0E0B38).copy(alpha = .68f)

private val AvailableColor = Color(0xFF1648D5).copy(alpha = .18f)
private val BookedColor = Color(0xFF0E0B38).copy(alpha = .72f)
private val SelectedColor = Color(0xFFFF8A3D)


// ------------------------------------------------------------
// SLOT MODEL
// ------------------------------------------------------------

private data class TurfSlot(
    val id: Int,
    val time: String,
    val booked: Boolean
)


// ------------------------------------------------------------
// 24 SLOTS
// ------------------------------------------------------------

private val turfSlots = listOf(

    TurfSlot(0, "12 AM", false),
    TurfSlot(1, "1 AM", false),
    TurfSlot(2, "2 AM", true),
    TurfSlot(3, "3 AM", false),

    TurfSlot(4, "4 AM", true),
    TurfSlot(5, "5 AM", false),
    TurfSlot(6, "6 AM", false),
    TurfSlot(7, "7 AM", true),

    TurfSlot(8, "8 AM", false),
    TurfSlot(9, "9 AM", false),
    TurfSlot(10, "10 AM", true),
    TurfSlot(11, "11 AM", false),

    TurfSlot(12, "12 PM", false),
    TurfSlot(13, "1 PM", true),
    TurfSlot(14, "2 PM", false),
    TurfSlot(15, "3 PM", false),

    TurfSlot(16, "4 PM", false),
    TurfSlot(17, "5 PM", true),
    TurfSlot(18, "6 PM", false),
    TurfSlot(19, "7 PM", false),

    TurfSlot(20, "8 PM", true),
    TurfSlot(21, "9 PM", false),
    TurfSlot(22, "10 PM", false),
    TurfSlot(23, "11 PM", false)
)


// ------------------------------------------------------------
// TURF BOOKING SCREEN
// ------------------------------------------------------------

@Composable
fun TurfBookingScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {

    var selectedDate by remember {
        mutableStateOf(Calendar.getInstance())
    }

    var selectedSlots by remember {
        mutableStateOf(setOf<Int>())
    }

    var showTerms by remember { mutableStateOf(false) }

    val pricePerHour = 800

    val totalPrice =
        selectedSlots.size * pricePerHour

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding(),
                contentPadding = PaddingValues(
                    bottom = 20.dp
                )
            ) {

                // ------------------------------------------------
                // TOP BAR
                // ------------------------------------------------

                item {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 14.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TurfWhite,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    onBackClick()
                                }
                        )

                        Spacer(
                            modifier = Modifier.width(16.dp)
                        )

                        Text(
                            text = "Book Turf",
                            color = TurfWhite,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ------------------------------------------------
                // TURF INFORMATION
                // ------------------------------------------------

                item {

                    TurfBookingHeader()

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }

                // ------------------------------------------------
                // DATE
                // ------------------------------------------------

                item {

                    SectionTitle(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Select Date"
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    DateSelector(
                        selectedDate = selectedDate,
                        onDateSelected = {
                            selectedDate = it
                            selectedSlots = emptySet()
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )
                }

                // ------------------------------------------------
                // SLOTS TITLE
                // ------------------------------------------------

                item {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        SectionTitle(
                            icon = Icons.Outlined.Schedule,
                            title = "Select Time Slot"
                        )

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "24 Slots",
                            color = TurfGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )
                }

                // ------------------------------------------------
                // LEGEND
                // ------------------------------------------------

                item {

                    SlotLegend()

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )
                }

                // ------------------------------------------------
                // 24 SLOTS
                // ------------------------------------------------

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        turfSlots.chunked(3).forEach { rowSlots ->
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
                                // Fill remaining space in the last row
                                repeat(3 - rowSlots.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "• Every booking comes with 55 minutes of active playtime",
                        color = TurfGray.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // ----------------------------------------------------
            // CONTINUE BUTTON
            // ----------------------------------------------------

            BottomBookingBar(
                selectedSlots = selectedSlots.size,
                totalPrice = totalPrice,
                onContinueClick = { showTerms = true }
            )
        }

        if (showTerms) {
            TermsAndPolicyBottomSheet(
                category = "TURF",
                onDismiss = { showTerms = false },
          onAccept = {
                    showTerms = false
                    onContinueClick()
                }
            )
        }
    }
}


// ------------------------------------------------------------
// TURF HEADER
// ------------------------------------------------------------

@Composable
private fun TurfBookingHeader() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        Text(
            text = "Green Arena Turf",
            color = TurfWhite,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "★ 4.7",
                color = TurfOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "  •  Football  •  5v5",
                color = TurfGray,
                fontSize = 14.sp
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = TurfOrange,
                modifier = Modifier.size(19.dp)
            )

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Text(
                text = "Chennai, Tamil Nadu",
                color = TurfGray,
                fontSize = 13.sp
            )
        }
    }
}


// ------------------------------------------------------------
// SECTION TITLE
// ------------------------------------------------------------

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {

    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TurfOrange,
            modifier = Modifier.size(20.dp)
        )

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = title,
            color = TurfWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// ------------------------------------------------------------
// DATE SELECTOR
// ------------------------------------------------------------

@Composable
private fun DateSelector(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        for (i in 0..6) {

            val date = Calendar.getInstance()

            date.add(
                Calendar.DAY_OF_YEAR,
                i
            )

            val selected =
                date.get(Calendar.YEAR) ==
                        selectedDate.get(Calendar.YEAR) &&
                        date.get(Calendar.DAY_OF_YEAR) ==
                        selectedDate.get(Calendar.DAY_OF_YEAR)

            DateItem(
                date = date,
                selected = selected,
                onClick = {
                    onDateSelected(date)
                }
            )
        }
    }
}


// ------------------------------------------------------------
// DATE ITEM
// ------------------------------------------------------------

@Composable
private fun DateItem(
    date: Calendar,
    selected: Boolean,
    onClick: () -> Unit
) {

    val dayName = SimpleDateFormat(
        "EEE",
        Locale.getDefault()
    ).format(date.time)

    val dayNumber =
        date.get(Calendar.DAY_OF_MONTH)

    val monthName = SimpleDateFormat(
        "MMM",
        Locale.getDefault()
    ).format(date.time)

    Column(
        modifier = Modifier
            .width(43.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(
                if (selected) {
                    TurfOrange
                } else {
                    TurfCardLight
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    TurfOrange
                } else {
                    Color(0xFF1648D5).copy(alpha = .38f)
                },
                shape = RoundedCornerShape(11.dp)
            )
            .clickable {
                onClick()
            }
            .padding(
                vertical = 9.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = dayName,
            color = if (selected) {
                Color.White
            } else {
                TurfGray
            },
            fontSize = 11.sp
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = dayNumber.toString(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = monthName,
            color = if (selected) {
                Color.White.copy(alpha = 0.85f)
            } else {
                TurfGray
            },
            fontSize = 9.sp
        )
    }
}


// ------------------------------------------------------------
// SLOT LEGEND
// ------------------------------------------------------------

@Composable
private fun SlotLegend() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        LegendItem(
            color = AvailableColor,
            text = "Available"
        )

        LegendItem(
            color = SelectedColor,
            text = "Selected"
        )

        LegendItem(
            color = BookedColor,
            text = "Booked"
        )
    }
}


// ------------------------------------------------------------
// LEGEND ITEM
// ------------------------------------------------------------

@Composable
private fun LegendItem(
    color: Color,
    text: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )

        Spacer(
            modifier = Modifier.width(5.dp)
        )

        Text(
            text = text,
            color = TurfGray,
            fontSize = 11.sp
        )
    }
}


// ------------------------------------------------------------
// TURF SLOT
// ------------------------------------------------------------

@Composable
private fun TurfSlotItem(
    slot: TurfSlot,
    selected: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor = when {

        slot.booked -> BookedColor

        selected -> SelectedColor

        else -> AvailableColor
    }

    val borderColor = when {

        slot.booked ->
            Color(0xFF242A35)

        selected ->
            Color(0xFFFF8A3D)

        else ->
            Color(0xFF1C5380)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                enabled = !slot.booked,
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = if (selected) {
                Icons.Rounded.CheckCircle
            } else {
                Icons.Rounded.Schedule
            },
            contentDescription = null,
            tint = if (slot.booked) {
                Color(0xFF535B69)
            } else {
                Color.White
            },
            modifier = Modifier.size(16.dp)
        )

        Spacer(
            modifier = Modifier.width(6.dp)
        )

        Column {

            Text(
                text = slot.time,
                color = if (slot.booked) {
                    Color(0xFF8B95A1)
                } else {
                    Color.White
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (slot.booked) {
                    "Booked"
                } else if (selected) {
                    "Selected"
                } else {
                    "Available"
                },
                color = if (slot.booked) {
                    Color(0xFF8B95A1).copy(alpha = 0.7f)
                } else if (selected) {
                    Color.White.copy(alpha = 0.9f)
                } else {
                    Color(0xFF9FC7EA)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// ------------------------------------------------------------
// BOTTOM BOOKING BAR
// ------------------------------------------------------------

@Composable
private fun BottomBookingBar(
    selectedSlots: Int,
    totalPrice: Int,
    onContinueClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF0E0B38).copy(alpha = .92f)
            )
            .navigationBarsPadding()
            .padding(
                horizontal = 18.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = if (selectedSlots == 0) {
                    "Select a slot"
                } else {
                    "$selectedSlots hour(s) selected"
                },
                color = TurfGray,
                fontSize = 11.sp
            )

            Text(
                text = "₹$totalPrice",
                color = TurfWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (selectedSlots > 0) {
                        TurfOrange
                    } else {
                        Color(0xFF082A82).copy(alpha = .55f)
                    }
                )
                .clickable(
                    enabled = selectedSlots > 0,
                    onClick = onContinueClick
                )
                .padding(
                    horizontal = 25.dp,
                    vertical = 13.dp
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Continue",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
