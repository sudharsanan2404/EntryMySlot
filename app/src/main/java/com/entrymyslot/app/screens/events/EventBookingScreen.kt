package com.entrymyslot.app.screens.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.*
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
import com.entrymyslot.app.screens.home.PopularEvent

// ------------------------------------------------------------
// COLORS
// ------------------------------------------------------------
private val EventBlueTop = Color(0xFF063DB5)
private val EventBlueBottom = Color(0xFF041F5D)
private val EventOrange = Color(0xFFFA580B)
private val EventWhite = Color.White
private val EventGray = Color(0xFFB8C0D0)
private val EventCard = Color(0xFF111D32)
private val EventCardLight = Color(0xFF142B58)

// ------------------------------------------------------------
// MODELS
// ------------------------------------------------------------
data class TicketTier(
    val id: String,
    val name: String,
    val price: Int,
    val description: String,
    val available: Int,
    val isSoldOut: Boolean = false
)

// ------------------------------------------------------------
// EVENT BOOKING SCREEN
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
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

    var selectedQuantities by remember { mutableStateOf(mapOf<String, Int>()) }
    
    val totalTickets = selectedQuantities.values.sum()
    val subtotal = tiers.sumOf { tier -> (selectedQuantities[tier.id] ?: 0) * tier.price }
    val convenienceFee = if (totalTickets > 0) 150 else 0
    val taxes = (subtotal * 0.05).toInt() // 5% tax
    val totalAmount = subtotal + convenienceFee + taxes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Event", color = EventWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = EventWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomBookingBar(totalTickets, totalAmount, onContinueClick, selectedQuantities)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(EventBlueTop, EventBlueBottom)))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Event Header
                item { EventHeader(event) }

                // 2. Section Title
                item {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Select Tickets", color = EventWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Choose your preferred seating category", color = EventGray, fontSize = 14.sp)
                    }
                }

                // 3. Venue Visualization
                item { VenueVisualization() }

                // 4. Ticket Tiers
                items(tiers) { tier ->
                    TicketTierCard(
                        tier = tier,
                        quantity = selectedQuantities[tier.id] ?: 0,
                        onQuantityChange = { newQty ->
                            selectedQuantities = selectedQuantities.toMutableMap().apply {
                                if (newQty > 0) put(tier.id, newQty) else remove(tier.id)
                            }
                        }
                    )
                }

                // 5. Booking Summary
                if (totalTickets > 0) {
                    item {
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
                
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun EventHeader(event: PopularEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventCard),
        border = BorderStroke(1.dp, Color(0xFF2A426B))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail Placeholder
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EventCardLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ConfirmationNumber, null, tint = EventGray, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(event.title, color = EventWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Sports Event", color = EventGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = EventOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.location, color = EventGray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = EventOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.date, color = EventGray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun VenueVisualization() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stage
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(30.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("STAGE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // VIP Area
        TierArea("VIP AREA", 0.5f)
        TierArea("PLATINUM AREA", 0.65f)
        TierArea("GOLD AREA", 0.8f)
        TierArea("SILVER AREA", 0.95f)
        
        // General Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )
        Text("GENERAL AREA", color = EventGray, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun TierArea(label: String, width: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(width)
                .height(20.dp)
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
        )
        Text(label, color = EventGray, fontSize = 8.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TicketTierCard(
    tier: TicketTier,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    val isSelected = quantity > 0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0038A8).copy(alpha = 0.2f) else EventCard
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected) EventOrange else if (tier.isSoldOut) Color.Transparent else Color(0xFF2A426B)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tier.name, color = if (tier.isSoldOut) EventGray else EventWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (tier.isSoldOut) {
                        Badge(
                            containerColor = Color.Red.copy(alpha = 0.2f),
                            contentColor = Color.Red,
                            modifier = Modifier.padding(start = 8.dp)
                        ) { Text("SOLD OUT") }
                    }
                }
                Text("₹${tier.price} / ticket", color = if (tier.isSoldOut) EventGray.copy(alpha = 0.5f) else EventOrange, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(tier.description, color = EventGray, fontSize = 12.sp)
                if (!tier.isSoldOut) {
                    Text("Only ${tier.available} tickets left", color = Color(0xFFF44336), fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (!tier.isSoldOut) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(EventCardLight, RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 0) onQuantityChange(quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = if (quantity > 0) EventOrange else EventGray, modifier = Modifier.size(18.dp))
                    }
                    
                    Text(
                        text = quantity.toString(),
                        color = EventWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = { if (quantity < tier.available) onQuantityChange(quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = if (quantity < tier.available) EventOrange else EventGray, modifier = Modifier.size(18.dp))
                    }
                }
            }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Text("Booking Summary", color = EventWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EventCard),
            border = BorderStroke(1.dp, Color(0xFF2A426B))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                tiers.forEach { tier ->
                    val qty = selectedQuantities[tier.id] ?: 0
                    if (qty > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(tier.name, color = EventWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("$qty × ₹${tier.price}", color = EventGray, fontSize = 12.sp)
                            }
                            Text("₹${qty * tier.price}", color = EventWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF293A59)))
                Spacer(modifier = Modifier.height(12.dp))

                SummaryPriceRow("Subtotal", "₹$subtotal")
                SummaryPriceRow("Convenience Fee", "₹$fee")
                SummaryPriceRow("Taxes", "₹$taxes")

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF293A59)))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", color = EventWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("₹$total", color = EventOrange, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun SummaryPriceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = EventGray, fontSize = 13.sp)
        Text(value, color = EventWhite, fontSize = 13.sp)
    }
}

@Composable
private fun BottomBookingBar(
    count: Int,
    total: Int,
    onContinueClick: (Map<String, Int>) -> Unit,
    quantities: Map<String, Int>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF061F58),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(if (count == 0) "Select tickets" else "$count Tickets", color = EventGray, fontSize = 12.sp)
                Text("₹$total", color = EventWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }

            Button(
                onClick = { onContinueClick(quantities) },
                enabled = count > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EventOrange,
                    disabledContainerColor = Color(0xFF4A5261)
                ),
                modifier = Modifier.height(52.dp).padding(start = 16.dp)
            ) {
                Text("Continue", color = EventWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
