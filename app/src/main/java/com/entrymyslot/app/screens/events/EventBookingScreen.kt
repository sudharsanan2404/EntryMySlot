package com.entrymyslot.app.screens.events

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.entrymyslot.app.core.components.TermsAndPolicyBottomSheet
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.screens.home.PopularEvent

private val BookingBackground = Color(0xFF061A38)
private val BookingSurface = Color(0xFF0B274F)
private val BookingSurfaceRaised = Color(0xFF0D2D5A)
private val BookingBorder = Color(0xFF24527D)
private val BookingAccent = Color(0xFFFA580B)
private val BookingPrimaryText = Color(0xFFF8FAFF)
private val BookingSecondaryText = Color(0xFFA8B8CF)
private val BookingMutedText = Color(0xFF7185A1)

data class TicketTier(
    val id: String,
    val name: String,
    val price: Int,
    val description: String,
    val available: Int,
    val isSoldOut: Boolean = false
)

@Composable
fun EventBookingScreen(
    event: PopularEvent,
    onBackClick: () -> Unit = {},
    onContinueClick: (Map<String, Int>) -> Unit = {}
) {
    val tiers = remember {
        listOf(
            TicketTier("vip", "VIP", 2500, "Premium seating with best stadium view", 18),
            TicketTier("platinum", "Platinum", 1800, "Excellent view from elevated platform", 45),
            TicketTier("gold", "Gold", 1200, "Good view from center stands", 120),
            TicketTier("silver", "Silver", 700, "Standard view from side stands", 200),
            TicketTier("general", "General", 400, "Entry level seating", 0, isSoldOut = true)
        )
    }

    var selectedQuantities by remember(event.id) {
        mutableStateOf(mapOf<String, Int>())
    }
    var showTerms by remember { mutableStateOf(false) }

    val totalTickets = selectedQuantities.values.sum()
    val subtotal = tiers.sumOf { tier ->
        (selectedQuantities[tier.id] ?: 0) * tier.price
    }
    val convenienceFee = if (totalTickets > 0) 150 else 0
    val taxes = (subtotal * 0.05).toInt()
    val totalAmount = subtotal + convenienceFee + taxes

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            BookingHeader(
                onBackClick = onBackClick,
                modifier = Modifier.statusBarsPadding()
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                item(key = "event_summary") {
                    BookingEventSummary(event = event)
                }

                item(key = "venue_map") {
                    EventSeatMap()
                }

                item(key = "ticket_heading") {
                    Column(modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)) {
                        Text(
                            text = "Select Tickets",
                            color = BookingPrimaryText,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Choose a category and the number of tickets",
                            color = BookingSecondaryText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                items(
                    items = tiers,
                    key = { tier -> tier.id }
                ) { tier ->
                    TicketTierCard(
                        tier = tier,
                        quantity = selectedQuantities[tier.id] ?: 0,
                        onQuantityChange = { newQuantity ->
                            selectedQuantities = selectedQuantities.toMutableMap().apply {
                                if (newQuantity > 0) {
                                    put(tier.id, newQuantity)
                                } else {
                                    remove(tier.id)
                                }
                            }
                        }
                    )
                }

                if (totalTickets > 0) {
                    item(key = "booking_summary") {
                        BookingSummary(
                            tiers = tiers,
                            selectedQuantities = selectedQuantities,
                            subtotal = subtotal,
                            fee = convenienceFee,
                            taxes = taxes,
                            total = totalAmount
                        )
                    }
                }

            }

            EventBookingBottomBar(
                count = totalTickets,
                total = totalAmount,
                onContinueClick = { showTerms = true }
            )
        }

        if (showTerms) {
            TermsAndPolicyBottomSheet(
                category = "EVENT",
                onDismiss = { showTerms = false },
                onAccept = {
                    showTerms = false
                    onContinueClick(selectedQuantities)
                }
            )
        }
    }
}

@Composable
private fun BookingHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookingBackButton(onClick = onBackClick)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "Book Event",
                color = BookingPrimaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Choose your tickets",
                color = BookingSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BookingBackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "bookingBackScale"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
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
            tint = BookingPrimaryText,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun EventSeatMap() {
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(BookingSurfaceRaised, BookingSurface)
                )
            )
            .border(1.dp, BookingBorder.copy(alpha = 0.75f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Choose your section", color = BookingPrimaryText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Pinch to zoom · Drag to move", color = BookingSecondaryText, fontSize = 11.sp)
            }
            Box(
                Modifier.clip(RoundedCornerShape(50))
                    .background(BookingAccent.copy(alpha = 0.14f))
                    .border(1.dp, BookingAccent.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("INTERACTIVE", color = BookingAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            "STAGE VIEW",
            color = BookingMutedText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp, bottom = 7.dp)
        )
        Box(
            Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(18.dp))
                .background(BookingBackground)
                .border(1.dp, BookingBorder.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTransformGestures(panZoomLock = true) { _, panChange, zoomChange, _ ->
                        val nextZoom = (zoom * zoomChange).coerceIn(1f, 3f)
                        zoom = nextZoom
                        val limitX = 145f * (nextZoom - 1f)
                        val limitY = 110f * (nextZoom - 1f)
                        pan = if (nextZoom <= 1.01f) {
                            Offset.Zero
                        } else {
                            Offset(
                                (pan.x + panChange.x).coerceIn(-limitX, limitX),
                                (pan.y + panChange.y).coerceIn(-limitY, limitY)
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.requiredSize(390.dp, 260.dp).graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = pan.x
                    translationY = pan.y
                }.padding(horizontal = 15.dp, vertical = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    Modifier.width(154.dp).height(30.dp)
                        .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = 22.dp, bottomEnd = 22.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFF793A), BookingAccent)))
                        .shadow(8.dp, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("STAGE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                SeatTierSection("VIP", "Closest view", BookingAccent, 210.dp, 6)
                SeatTierSection("PLATINUM", "Premium", Color(0xFF6E8FE0), 255.dp, 8)
                SeatTierSection("GOLD", "Great view", Color(0xFFD7A340), 300.dp, 10)
                SeatTierSection("SILVER", "Standard", Color(0xFF8195AD), 335.dp, 12)
                SeatTierSection("GENERAL", "Open seating", BookingMutedText, 360.dp, 14)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(BookingAccent))
            Text("  Section color", color = BookingSecondaryText, fontSize = 10.sp)
            Box(Modifier.padding(start = 16.dp).size(7.dp).clip(CircleShape).background(BookingPrimaryText.copy(alpha = 0.8f)))
            Text("  Available seats", color = BookingSecondaryText, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SeatTierSection(
    label: String,
    detail: String,
    color: Color,
    width: androidx.compose.ui.unit.Dp,
    seatCount: Int
) {
    Column(
        Modifier.width(width).height(34.dp).clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(17.dp).clip(CircleShape).background(color))
            Column(Modifier.padding(start = 6.dp)) {
                Text(label, color = BookingPrimaryText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(detail, color = BookingSecondaryText, fontSize = 6.sp)
            }
            Spacer(Modifier.weight(1f))
            repeat(seatCount) {
                Box(Modifier.padding(horizontal = 1.5.dp).size(4.dp).clip(CircleShape).background(BookingPrimaryText.copy(alpha = 0.82f)))
            }
        }
    }
}

@Composable
private fun BookingEventSummary(event: PopularEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 78.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF123E70), Color(0xFF081F42))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (event.imageUrl != null) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = event.title,
                    tint = BookingAccent,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                color = BookingPrimaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 21.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            CompactMetadataRow(
                icon = Icons.Rounded.CalendarToday,
                text = event.date
            )
            Spacer(modifier = Modifier.height(4.dp))
            CompactMetadataRow(
                icon = Icons.Rounded.LocationOn,
                text = event.location
            )
        }
    }
}

@Composable
private fun CompactMetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BookingAccent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = BookingSecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TicketTierCard(
    tier: TicketTier,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    val isSelected = quantity > 0
    val containerColor by animateColorAsState(
        targetValue = when {
            tier.isSoldOut -> BookingSurface.copy(alpha = 0.55f)
            isSelected -> BookingAccent.copy(alpha = 0.12f)
            else -> BookingSurface
        },
        animationSpec = tween(durationMillis = 160),
        label = "ticketTierSurface"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            tier.isSoldOut -> BookingBorder.copy(alpha = 0.32f)
            isSelected -> BookingAccent
            else -> BookingBorder.copy(alpha = 0.82f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "ticketTierBorder"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 5.dp else 2.dp,
        animationSpec = tween(durationMillis = 160),
        label = "ticketTierElevation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.16f),
                spotColor = Color.Black.copy(alpha = 0.24f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(
                BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                RoundedCornerShape(14.dp)
            )
            .semantics {
                selected = isSelected
                stateDescription = when {
                    tier.isSoldOut -> "Sold out"
                    quantity == 0 -> "No tickets selected"
                    else -> "$quantity tickets selected"
                }
            }
            .padding(13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tier.name,
                        color = if (tier.isSoldOut) BookingMutedText else BookingPrimaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (tier.isSoldOut) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SOLD OUT",
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BookingMutedText.copy(alpha = 0.14f))
                                .border(
                                    width = 1.dp,
                                    color = BookingMutedText.copy(alpha = 0.28f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            color = BookingSecondaryText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.7.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${tier.price} / ticket",
                    color = if (tier.isSoldOut) BookingMutedText else BookingAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BookingAccent.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "SELECTED",
                        color = BookingAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = tier.description,
            color = if (tier.isSoldOut) {
                BookingMutedText.copy(alpha = 0.72f)
            } else {
                BookingSecondaryText
            },
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(9.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tier.isSoldOut) {
                Text(
                    text = "Currently unavailable",
                    color = BookingMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ConfirmationNumber,
                        contentDescription = null,
                        tint = BookingAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Only ${tier.available} tickets left",
                        color = BookingSecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                QuantitySelector(
                    quantity = quantity,
                    canDecrease = quantity > 0,
                    canIncrease = quantity < tier.available,
                    onDecrease = { onQuantityChange(quantity - 1) },
                    onIncrease = { onQuantityChange(quantity + 1) }
                )
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BookingSurfaceRaised)
            .border(
                BorderStroke(1.dp, BookingBorder.copy(alpha = 0.72f)),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrease,
            enabled = canDecrease,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease ticket quantity",
                tint = if (canDecrease) BookingAccent else BookingMutedText,
                modifier = Modifier.size(17.dp)
            )
        }

        Box(
            modifier = Modifier.width(30.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = quantity,
                label = "ticketQuantity"
            ) { displayedQuantity ->
                Text(
                    text = displayedQuantity.toString(),
                    color = BookingPrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        IconButton(
            onClick = onIncrease,
            enabled = canIncrease,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase ticket quantity",
                tint = if (canIncrease) BookingAccent else BookingMutedText,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun BookingSummary(
    tiers: List<TicketTier>,
    selectedQuantities: Map<String, Int>,
    subtotal: Int,
    fee: Int,
    taxes: Int,
    total: Int
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Booking Summary",
            color = BookingPrimaryText,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(BookingSurface)
                .border(
                    BorderStroke(1.dp, BookingBorder.copy(alpha = 0.82f)),
                    RoundedCornerShape(18.dp)
                )
                .padding(17.dp)
        ) {
            tiers.forEach { tier ->
                val quantity = selectedQuantities[tier.id] ?: 0
                if (quantity > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = tier.name,
                                color = BookingPrimaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$quantity × ₹${tier.price}",
                                color = BookingSecondaryText,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "₹${quantity * tier.price}",
                            color = BookingPrimaryText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            SummaryDivider()
            SummaryPriceRow(label = "Subtotal", value = "₹$subtotal")
            SummaryPriceRow(label = "Convenience Fee", value = "₹$fee")
            SummaryPriceRow(label = "Taxes", value = "₹$taxes")
            SummaryDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    color = BookingPrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹$total",
                    color = BookingAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun SummaryDivider() {
    Spacer(modifier = Modifier.height(11.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BookingBorder.copy(alpha = 0.46f))
    )
    Spacer(modifier = Modifier.height(11.dp))
}

@Composable
private fun SummaryPriceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = BookingSecondaryText,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = BookingPrimaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EventBookingBottomBar(
    count: Int,
    total: Int,
    onContinueClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed && count > 0) 0.975f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "continueButtonScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(BookingBackground.copy(alpha = 0.98f))
            .border(
                width = 1.dp,
                color = BookingBorder.copy(alpha = 0.58f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (count == 0) "Select tickets" else "$count Tickets",
                color = BookingSecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "₹$total",
                color = BookingPrimaryText,
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
                    if (count > 0) BookingAccent else BookingSurfaceRaised
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (count > 0) {
                            BookingAccent
                        } else {
                            BookingBorder.copy(alpha = 0.68f)
                        }
                    ),
                    RoundedCornerShape(13.dp)
                )
                .clickable(
                    enabled = count > 0,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = "Continue",
                    onClick = onContinueClick
                )
                .padding(horizontal = 29.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Continue",
                color = if (count > 0) Color.White else BookingMutedText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
