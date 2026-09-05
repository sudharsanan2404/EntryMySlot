package com.entrymyslot.app.screens.manager

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.data.FakeData
import kotlin.math.roundToInt

private val Orange = Color(0xFFFA580B)
private val Background = Color(0xFF061A38)
private val SurfaceBlue = Color(0xFF0B274F)
private val RaisedBlue = Color(0xFF0E315E)
private val Edge = Color(0xFF3976A8)
private val White = Color(0xFFF8FAFF)
private val Secondary = Color(0xFFA8B8CF)
private val Green = Color(0xFF22C55E)

private enum class ManagerArea(val label: String, val icon: ImageVector) {
    EVENT("Events", Icons.Rounded.Event), TURF("Turf", Icons.Rounded.SportsSoccer)
}

private enum class ManagerPage(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Rounded.Dashboard), BOOKINGS("Bookings", Icons.Rounded.CalendarMonth),
    OFFLINE("Book", Icons.Rounded.Add), SCAN("Scan", Icons.Rounded.QrCodeScanner)
}

private data class OfflineTicket(val code: String, val customer: String, val venue: String, val area: ManagerArea)

@Composable
fun ManagerDashboardScreen(onBackClick: () -> Unit) {
    var area by remember { mutableStateOf(ManagerArea.EVENT) }
    var page by remember { mutableStateOf(ManagerPage.OVERVIEW) }
    val offlineTickets = remember { mutableStateListOf<OfflineTicket>() }
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF092E9A), Color(0xFF071F58), Background))
        )
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 92.dp)) {
            ManagerHeader(onBackClick)
            AreaSelector(area) { area = it }
            Crossfade(page, label = "manager-page") { selectedPage ->
                when (selectedPage) {
                    ManagerPage.OVERVIEW -> ManagerOverview(area)
                    ManagerPage.BOOKINGS -> ManagerBookings(area, offlineTickets)
                    ManagerPage.OFFLINE -> OfflineBookingPage(area) { offlineTickets.add(0, it) }
                    ManagerPage.SCAN -> TicketScannerPage(offlineTickets.firstOrNull()?.code.orEmpty())
                }
            }
        }
        ManagerNavigation(page, { page = it }, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ManagerHeader(onBackClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = White) }
        Column(Modifier.weight(1f)) {
            Text("Manager workspace", color = White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("EVENTS & TURF OPERATIONS", color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun AreaSelector(selected: ManagerArea, onSelect: (ManagerArea) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp).clip(RoundedCornerShape(16.dp))
            .background(SurfaceBlue.copy(alpha = .86f)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ManagerArea.entries.forEach { area ->
            val active = area == selected
            Row(
                Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (active) Orange else Color.Transparent).clickable { onSelect(area) },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
            ) {
                Icon(area.icon, null, tint = if (active) White else Secondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text(area.label, color = if (active) White else Secondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ManagerOverview(area: ManagerArea) {
    val event = area == ManagerArea.EVENT
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(if (event) "Event operations" else "Turf operations", color = White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Everything happening today, at a glance.", color = Secondary, fontSize = 12.sp)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("TODAY", if (event) "3 events" else "18 slots", Icons.Rounded.CalendarMonth, Orange, Modifier.weight(1f))
                MetricCard("CHECKED IN", if (event) "128" else "11", Icons.Rounded.CheckCircle, Green, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("EXPECTED", if (event) "214 guests" else "7 teams", Icons.Rounded.Groups, Color(0xFF60A5FA), Modifier.weight(1f))
                MetricCard("UP NEXT", if (event) "6:30 PM" else "5:00 PM", Icons.Rounded.Schedule, Color(0xFFFBBF24), Modifier.weight(1f))
            }
        }
        item { SectionTitle("Next activity") }
        item {
            ActivityCard(
                if (event) "Live Cricket Championship" else "Green Arena Turf",
                if (event) "Gate opens at 5:30 PM · 214 attendees" else "5:00 PM – 6:00 PM · Football",
                if (event) "GATE A" else "TURF 01"
            )
        }
    }
}

@Composable
private fun ManagerBookings(area: ManagerArea, offlineTickets: List<OfflineTicket>) {
    val preview = FakeData.bookings.filter { if (area == ManagerArea.EVENT) it.type.name == "EVENT" else it.type.name == "TURF" }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("${area.label} bookings", color = White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Online reservations and manager walk-ins.", color = Secondary, fontSize = 12.sp)
        }
        items(offlineTickets.filter { it.area == area }) { BookingCard(it.customer, it.venue, it.code, "OFFLINE", Green) }
        items(preview) {
            BookingCard(
                it.title.takeUnless { title -> title == "Booking" } ?: FakeData.getItemById(it.itemId)?.title.orEmpty(),
                it.dateTime, it.bookingReference, "ONLINE", Orange
            )
        }
    }
}

@Composable
private fun OfflineBookingPage(area: ManagerArea, onCreated: (OfflineTicket) -> Unit) {
    val venues = if (area == ManagerArea.EVENT) FakeData.events.map { it.title } else FakeData.turfs.map { it.title }
    var selectedVenue by remember(area) { mutableStateOf(venues.firstOrNull().orEmpty()) }
    var customer by remember(area) { mutableStateOf("") }
    var phone by remember(area) { mutableStateOf("") }
    var quantity by remember(area) { mutableStateOf("1") }
    var createdCode by remember(area) { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            Text("Book for a walk-in", color = White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Create an offline ${area.label.lowercase()} booking and issue a ticket code.", color = Secondary, fontSize = 12.sp)
        }
        item { SectionTitle(if (area == ManagerArea.EVENT) "Choose event" else "Choose turf") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                venues.take(4).forEach { venue ->
                    val selected = venue == selectedVenue
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                            .background(if (selected) Orange.copy(alpha = .14f) else SurfaceBlue)
                            .border(1.dp, if (selected) Orange else Edge.copy(alpha = .25f), RoundedCornerShape(13.dp))
                            .clickable { selectedVenue = venue }.padding(12.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(area.icon, null, tint = if (selected) Orange else Secondary, modifier = Modifier.size(18.dp))
                        Text(venue, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 9.dp).weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (selected) Icon(Icons.Rounded.CheckCircle, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        item { ManagerField(customer, { customer = it }, "Customer name") }
        item { ManagerField(phone, { phone = it }, "Phone number") }
        item { ManagerField(quantity, { quantity = it.filter(Char::isDigit).take(2) }, if (area == ManagerArea.EVENT) "Ticket quantity" else "Hours") }
        item {
            Button(
                onClick = {
                    val prefix = if (area == ManagerArea.EVENT) "EVT" else "TRF"
                    val code = "EMS-$prefix-${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}"
                    createdCode = code
                    onCreated(OfflineTicket(code, customer.trim(), selectedVenue, area))
                },
                enabled = customer.isNotBlank() && phone.isNotBlank() && (quantity.toIntOrNull() ?: 0) > 0,
                modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange), shape = RoundedCornerShape(15.dp)
            ) {
                Icon(Icons.Rounded.ConfirmationNumber, null)
                Text("Create offline ticket", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            }
        }
        createdCode?.let { code -> item { GeneratedTicket(code) } }
    }
}

@Composable
private fun TicketScannerPage(fallbackCode: String) {
    var scanning by remember { mutableStateOf(false) }
    var code by remember(fallbackCode) { mutableStateOf(fallbackCode) }
    var result by remember { mutableStateOf<Boolean?>(null) }
    val transition = rememberInfiniteTransition(label = "scan-line")
    val scanPosition by transition.animateFloat(-72f, 72f, infiniteRepeatable(tween(1400), repeatMode = RepeatMode.Reverse), label = "scan-position")
    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
                Text("Scan ticket", color = White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Point the camera at a ticket or verify its code manually.", color = Secondary, fontSize = 12.sp)
            }
        }
        item {
            Box(
                Modifier.size(220.dp).clip(RoundedCornerShape(26.dp)).background(Color(0xFF031127))
                    .border(1.dp, Orange.copy(alpha = .65f), RoundedCornerShape(26.dp)), contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.QrCodeScanner, null, tint = Secondary.copy(alpha = .35f), modifier = Modifier.size(92.dp))
                if (scanning) Box(Modifier.fillMaxWidth(.78f).height(2.dp).offset { IntOffset(0, scanPosition.roundToInt()) }.background(Orange))
                Text(if (scanning) "LOOKING FOR CODE" else "CAMERA READY", color = if (scanning) Orange else Secondary, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
            }
        }
        item {
            Button(onClick = { scanning = !scanning }, colors = ButtonDefaults.buttonColors(containerColor = if (scanning) RaisedBlue else Orange), shape = RoundedCornerShape(13.dp)) {
                Text(if (scanning) "Stop camera scan" else "Start camera scan", fontWeight = FontWeight.Bold)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(Edge.copy(alpha = .3f)))
                Text("  MANUAL CODE  ", color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Box(Modifier.weight(1f).height(1.dp).background(Edge.copy(alpha = .3f)))
            }
        }
        item { ManagerField(code, { code = it.uppercase(); result = null }, "EMS ticket code") }
        item {
            Button(
                onClick = { result = code.trim().startsWith("EMS-") && code.trim().length >= 10 }, enabled = code.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(15.dp)
            ) { Text("Validate & check in", fontWeight = FontWeight.Black) }
        }
        result?.let { valid -> item { ValidationResult(valid, code) } }
    }
}

@Composable
private fun ManagerField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value, onValueChange, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = White, unfocusedTextColor = White, focusedLabelColor = Orange, unfocusedLabelColor = Secondary,
            focusedBorderColor = Orange, unfocusedBorderColor = Edge.copy(alpha = .35f), cursorColor = Orange,
            focusedContainerColor = SurfaceBlue.copy(alpha = .65f), unfocusedContainerColor = SurfaceBlue.copy(alpha = .65f)
        )
    )
}

@Composable
private fun GeneratedTicket(code: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Green.copy(alpha = .12f)), border = BorderStroke(1.dp, Green.copy(alpha = .45f))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.CheckCircle, null, tint = Green, modifier = Modifier.size(28.dp))
            Text("Ticket created", color = White, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 7.dp))
            Text(code, color = Green, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(top = 7.dp))
            Text("Use this code in Scan if camera validation is unavailable.", color = Secondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun ValidationResult(valid: Boolean, code: String) {
    val color = if (valid) Green else Color(0xFFEF4444)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = .12f))
            .border(1.dp, color.copy(alpha = .4f), RoundedCornerShape(16.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (valid) Icons.Rounded.CheckCircle else Icons.Rounded.QrCodeScanner, null, tint = color)
        Column(Modifier.padding(start = 11.dp)) {
            Text(if (valid) "Ticket accepted" else "Code not recognized", color = White, fontWeight = FontWeight.Black)
            Text(if (valid) "$code · Customer checked in" else "Enter a code beginning with EMS-", color = Secondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SurfaceBlue), border = BorderStroke(1.dp, Edge.copy(alpha = .22f))) {
        Column(Modifier.padding(15.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(21.dp))
            Text(value, color = White, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
            Text(label, color = Secondary, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
        }
    }
}

@Composable
private fun ActivityCard(title: String, detail: String, badge: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceBlue)
            .border(1.dp, Edge.copy(alpha = .24f), RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Orange.copy(alpha = .14f)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Event, null, tint = Orange) }
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Text(title, color = White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, color = Secondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(badge, color = Orange, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BookingCard(title: String, detail: String, code: String, source: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceBlue)
            .border(1.dp, Edge.copy(alpha = .2f), RoundedCornerShape(16.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(source, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(title, color = White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, color = Secondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(code.takeLast(8), color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun SectionTitle(title: String) { Text(title.uppercase(), color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }

@Composable
private fun ManagerNavigation(selected: ManagerPage, onSelect: (ManagerPage) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), color = Color(0xFF082145),
        shape = RoundedCornerShape(24.dp), shadowElevation = 16.dp, border = BorderStroke(1.dp, Edge.copy(alpha = .32f))
    ) {
        Row(Modifier.fillMaxWidth().height(66.dp).padding(horizontal = 5.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            ManagerPage.entries.forEach { page ->
                val active = page == selected
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).clickable { onSelect(page) }.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(page.icon, null, tint = if (active) Orange else Secondary, modifier = Modifier.size(21.dp))
                    Text(page.label, color = if (active) Orange else Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 3.dp))
                }
            }
        }
    }
}
