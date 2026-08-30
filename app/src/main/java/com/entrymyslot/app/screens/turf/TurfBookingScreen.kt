package com.entrymyslot.app.screens.turf

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.core.components.TermsAndPolicyBottomSheet
import com.entrymyslot.app.screens.home.GlowBackground
import java.text.SimpleDateFormat
import java.util.Calendar

private val TurfBookingBackground = Color(0xFF061A38)
private val TurfBookingSurface = Color(0xFF0B274F)
private val TurfBookingSurfaceRaised = Color(0xFF0D2D5A)
private val TurfBookingBorder = Color(0xFF24527D)
private val TurfBookingAccent = Color(0xFFFA580B)
private val TurfBookingPrimaryText = Color(0xFFF8FAFF)
private val TurfBookingSecondaryText = Color(0xFFA8B8CF)
private val TurfBookingMutedText = Color(0xFF7185A1)
private val AvailableSurface = Color(0xFF10345D)
private val AvailableBorder = Color(0xFF3471A3)
private val BookedSurface = Color(0xFF09182D)
private val BookedBorder = Color(0xFF1A3049)

private data class TurfSlot(
    val id: Int,
    val time: String,
    val booked: Boolean
)

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
    val totalPrice = selectedSlots.size * pricePerHour

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            TurfBookingTopBar(
                onBackClick = onBackClick,
                modifier = Modifier.statusBarsPadding()
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item(key = "venue_summary") {
                    VenueSummary()
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item(key = "date_selector") {
                    BookingSectionHeader(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Select Date"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DateSelector(
                        selectedDate = selectedDate,
                        onDateSelected = {
                            selectedDate = it
                            selectedSlots = emptySet()
                        }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }

                item(key = "slot_header") {
                    BookingSectionHeader(
                        icon = Icons.Outlined.Schedule,
                        title = "Select Time Slot",
                        trailingText = "24 Slots"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SlotLegend()
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item(key = "slot_grid") {
                    SlotGrid(
                        selectedSlots = selectedSlots,
                        onSlotClick = { slot ->
                            if (!slot.booked) {
                                selectedSlots = if (selectedSlots.contains(slot.id)) {
                                    selectedSlots - slot.id
                                } else {
                                    selectedSlots + slot.id
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BookingNote()
                }
            }

            TurfBottomBookingBar(
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

@Composable
private fun TurfBookingTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PremiumBackButton(onClick = onBackClick)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "Book Turf",
                color = TurfBookingPrimaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Choose your play time",
                color = TurfBookingSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PremiumBackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "turfBookingBackScale"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(TurfBookingSurface.copy(alpha = 0.94f))
            .border(BorderStroke(1.dp, TurfBookingBorder), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Go back",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = TurfBookingPrimaryText,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun VenueSummary() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Green Arena Turf",
            color = TurfBookingPrimaryText,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "★ 4.7",
                color = TurfBookingAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "  •  Football  •  5v5",
                color = TurfBookingSecondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = TurfBookingAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Chennai, Tamil Nadu",
                color = TurfBookingSecondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BookingSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailingText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TurfBookingAccent.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TurfBookingAccent,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = TurfBookingPrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TurfBookingSurfaceRaised)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                color = TurfBookingSecondaryText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0..6) {
            val date = Calendar.getInstance()
            date.add(Calendar.DAY_OF_YEAR, i)

            val selected = date.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                date.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)

            DateItem(
                date = date,
                selected = selected,
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DateItem(
    date: Calendar,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val dayName = SimpleDateFormat("EEE", locale).format(date.time)
    val dayNumber = date.get(Calendar.DAY_OF_MONTH)
    val monthName = SimpleDateFormat("MMM", locale).format(date.time)
    val spokenDate = SimpleDateFormat("EEEE, MMMM d", locale).format(date.time)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "datePressScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) TurfBookingAccent else Color.Transparent,
        animationSpec = tween(durationMillis = 160),
        label = "dateContainerColor"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 160),
        label = "dateElevation"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = elevation, shape = RoundedCornerShape(11.dp))
            .clip(RoundedCornerShape(11.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (selected) {
                    TurfBookingAccent
                } else {
                    TurfBookingBorder.copy(alpha = 0.48f)
                },
                shape = RoundedCornerShape(11.dp)
            )
            .semantics(mergeDescendants = true) {
                contentDescription = spokenDate
                this.selected = selected
                stateDescription = if (selected) "Selected" else "Not selected"
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Select $spokenDate",
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName,
            color = if (selected) {
                Color.White.copy(alpha = 0.88f)
            } else {
                TurfBookingSecondaryText
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dayNumber.toString(),
            color = TurfBookingPrimaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = monthName,
            color = if (selected) {
                Color.White.copy(alpha = 0.82f)
            } else {
                TurfBookingMutedText
            },
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SlotLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            fillColor = AvailableSurface,
            borderColor = AvailableBorder,
            text = "Available"
        )
        LegendItem(
            fillColor = TurfBookingAccent,
            borderColor = TurfBookingAccent,
            text = "Selected"
        )
        LegendItem(
            fillColor = BookedSurface,
            borderColor = BookedBorder,
            text = "Booked"
        )
    }
}

@Composable
private fun LegendItem(
    fillColor: Color,
    borderColor: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(fillColor)
                .border(1.dp, borderColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = TurfBookingSecondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SlotGrid(
    selectedSlots: Set<Int>,
    onSlotClick: (TurfSlot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        turfSlots.chunked(3).forEach { rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                rowSlots.forEach { slot ->
                    TurfSlotItem(
                        slot = slot,
                        selected = selectedSlots.contains(slot.id),
                        onClick = { onSlotClick(slot) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowSlots.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TurfSlotItem(
    slot: TurfSlot,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = when {
        slot.booked -> "Booked"
        selected -> "Selected"
        else -> "Available"
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val selectionScale = remember { Animatable(1f) }

    LaunchedEffect(selected) {
        if (selected) {
            selectionScale.snapTo(0.94f)
            selectionScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 170)
            )
        } else {
            selectionScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 120)
            )
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "slotPressScale"
    )
    val containerColor by animateColorAsState(
        targetValue = when {
            slot.booked -> BookedSurface
            selected -> TurfBookingAccent
            else -> AvailableSurface
        },
        animationSpec = tween(durationMillis = 170),
        label = "slotContainerColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            slot.booked -> BookedBorder
            selected -> TurfBookingAccent
            else -> AvailableBorder
        },
        animationSpec = tween(durationMillis = 170),
        label = "slotBorderColor"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 6.dp else 1.dp,
        animationSpec = tween(durationMillis = 170),
        label = "slotElevation"
    )

    Box(
        modifier = modifier
            .height(58.dp)
            .graphicsLayer {
                val combinedScale = selectionScale.value * pressScale
                scaleX = combinedScale
                scaleY = combinedScale
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(11.dp),
                ambientColor = if (selected) {
                    TurfBookingAccent.copy(alpha = 0.20f)
                } else {
                    Color.Black.copy(alpha = 0.12f)
                },
                spotColor = if (selected) {
                    TurfBookingAccent.copy(alpha = 0.28f)
                } else {
                    Color.Black.copy(alpha = 0.18f)
                }
            )
            .clip(RoundedCornerShape(11.dp))
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(11.dp))
            .clickable(
                enabled = !slot.booked,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = if (selected) "Deselect ${slot.time}" else "Select ${slot.time}",
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "${slot.time}, $status"
                stateDescription = status
                this.selected = selected
                role = Role.Button
                if (slot.booked) disabled()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = slot.time,
                color = if (slot.booked) TurfBookingMutedText else TurfBookingPrimaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = status,
                color = when {
                    slot.booked -> TurfBookingMutedText.copy(alpha = 0.70f)
                    selected -> Color.White.copy(alpha = 0.90f)
                    else -> TurfBookingSecondaryText
                },
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )
        }

        AnimatedVisibility(
            visible = selected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp),
            enter = fadeIn(tween(120)) + scaleIn(tween(150), initialScale = 0.72f),
            exit = fadeOut(tween(90)) + scaleOut(tween(100), targetScale = 0.72f)
        ) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun BookingNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(TurfBookingSurface.copy(alpha = 0.64f))
            .border(
                BorderStroke(1.dp, TurfBookingBorder.copy(alpha = 0.46f)),
                RoundedCornerShape(13.dp)
            )
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = TurfBookingAccent,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Every booking comes with 55 minutes of active playtime",
            modifier = Modifier.weight(1f),
            color = TurfBookingSecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun TurfBottomBookingBar(
    selectedSlots: Int,
    totalPrice: Int,
    onContinueClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed && selectedSlots > 0) 0.975f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "turfContinueScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(TurfBookingBackground.copy(alpha = 0.98f))
            .border(
                BorderStroke(1.dp, TurfBookingBorder.copy(alpha = 0.58f)),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (selectedSlots) {
                    0 -> "Select a slot"
                    1 -> "1 hour selected"
                    else -> "$selectedSlots hours selected"
                },
                color = TurfBookingSecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "₹$totalPrice",
                color = TurfBookingPrimaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .clip(RoundedCornerShape(13.dp))
                .background(
                    if (selectedSlots > 0) {
                        TurfBookingAccent
                    } else {
                        TurfBookingSurfaceRaised
                    }
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (selectedSlots > 0) {
                            TurfBookingAccent
                        } else {
                            TurfBookingBorder.copy(alpha = 0.68f)
                        }
                    ),
                    RoundedCornerShape(13.dp)
                )
                .clickable(
                    enabled = selectedSlots > 0,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = "Continue",
                    onClick = onContinueClick
                )
                .padding(horizontal = 27.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Continue",
                color = if (selectedSlots > 0) Color.White else TurfBookingMutedText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
