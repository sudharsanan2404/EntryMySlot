package com.entrymyslot.app.screens.manager

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.Booking
import com.entrymyslot.app.screens.home.GlowBackground
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
    DASHBOARD("Console", Icons.Rounded.Analytics),
    BOOK("Entry", Icons.Rounded.Add),
    SCAN("Scan", Icons.Rounded.QrCodeScanner),
    HISTORY("Activity", Icons.Rounded.History)
}

private data class ActivityFeedItem(
    val id: String,
    val type: String, // "CHECK-IN", "BOOKING", "CANCEL"
    val title: String,
    val detail: String,
    val time: String,
    val amount: String? = null,
    val isFinancial: Boolean = false
)

@Composable
fun ManagerDashboardScreen(onBackClick: () -> Unit) {
    var area by remember { mutableStateOf(ManagerArea.EVENT) }
    var page by remember { mutableStateOf(ManagerPage.DASHBOARD) }
    
    val activityFeed = remember { 
        mutableStateListOf(
            ActivityFeedItem("1", "CHECK-IN", "Navaneethan", "Live Cricket Championship · Gate A", "2 mins ago"),
            ActivityFeedItem("2", "BOOKING", "Walk-in Customer", "Green Arena Turf · 2 Hours", "15 mins ago", "₹1,600", isFinancial = true),
            ActivityFeedItem("3", "CHECK-IN", "Priya Sharma", "Arijit Singh Live · VIP", "45 mins ago"),
            ActivityFeedItem("4", "CANCEL", "Suresh Kumar", "Elite Badminton · Canceled by User", "1 hour ago"),
            ActivityFeedItem("5", "BOOKING", "Online Payment", "Interstellar · IMAX", "2 hours ago", "₹360", isFinancial = true),
            ActivityFeedItem("6", "BOOKING", "Cash Collection", "Football Turf · Spot Booking", "3 hours ago", "₹800", isFinancial = true)
        )
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF092E9A), Color(0xFF071F58), Background))
        )
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 92.dp)) {
            ManagerHeader(onBackClick)
            if (page != ManagerPage.BOOK) AreaSelector(area) { area = it }
            
            Crossfade(page, label = "manager-page") { selectedPage ->
                when (selectedPage) {
                    ManagerPage.DASHBOARD -> DashboardOverview(area, activityFeed, onViewAll = { page = ManagerPage.HISTORY })
                    ManagerPage.BOOK -> OfflineBookingPage { ticket ->
                        activityFeed.add(0, ActivityFeedItem(
                            id = ticket.code,
                            type = "BOOKING",
                            title = ticket.customer,
                            detail = "${ticket.venue} · Walk-in",
                            time = "Just now",
                            amount = "₹800",
                            isFinancial = true
                        ))
                    }
                    ManagerPage.SCAN -> TicketScannerPage { code ->
                        activityFeed.add(0, ActivityFeedItem(
                            id = code,
                            type = "CHECK-IN",
                            title = "Valid Entry",
                            detail = "Ticket #$code Verified",
                            time = "Just now"
                        ))
                    }
                    ManagerPage.HISTORY -> HistoryPage(activityFeed)
                }
            }
        }
        ManagerNavigation(page, {
            if (it == ManagerPage.BOOK) area = ManagerArea.TURF
            page = it
        }, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ManagerHeader(onBackClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = White) }
        Column(Modifier.weight(1f)) {
            Text("Manager Console", color = White, fontSize = 19.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text("EVENTS & TURF OPERATIONS", color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
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
private fun DashboardOverview(area: ManagerArea, feed: List<ActivityFeedItem>, onViewAll: () -> Unit) {
    val isEvent = area == ManagerArea.EVENT
    val financialFeed = feed.filter { it.isFinancial }
    
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Today’s overview", color = White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Here is today’s venue activity.", color = Secondary, fontSize = 12.sp)
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RaisedBlue),
                border = BorderStroke(1.dp, Edge.copy(alpha = .3f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("NET REVENUE (24H)", color = Secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("₹${if(isEvent) "42,800" else "12,600"}", color = White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RevenueSplitCard("Online", if(isEvent) "₹38,200" else "₹8,400", Orange, Modifier.weight(1f))
                        RevenueSplitCard("Offline", if(isEvent) "₹4,600" else "₹4,200", Green, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("GUESTS", if(isEvent) "128 / 214" else "11 / 18", Icons.Rounded.Groups, Color(0xFF60A5FA), Modifier.weight(1f))
                MetricCard("PENDING", "₹2,400", Icons.Rounded.Schedule, Color(0xFFFBBF24), Modifier.weight(1f))
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Financial Ledger", color = White, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                Text(
                    "View All History", 
                    color = Orange, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onViewAll() }.padding(8.dp)
                )
            }
        }

        if (financialFeed.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions logged yet", color = Secondary, fontSize = 13.sp)
                }
            }
        } else {
            items(financialFeed.take(4)) { activity ->
                ActivityFeedCard(activity)
            }
        }
    }
}

@Composable
private fun HistoryPage(feed: List<ActivityFeedItem>) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Check-ins", "Financials", "Canceled")
    
    val filteredFeed = when(selectedTab) {
        1 -> feed.filter { it.type == "CHECK-IN" }
        2 -> feed.filter { it.isFinancial }
        3 -> feed.filter { it.type == "CANCEL" }
        else -> feed
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            Text("Activity History", color = White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Real-time audit log of all venue actions.", color = Secondary, fontSize = 12.sp)
        }
        
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Orange,
            edgePadding = 18.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Orange
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 13.sp, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Medium) },
                    selectedContentColor = Orange,
                    unselectedContentColor = Secondary
                )
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredFeed.isEmpty()) {
                item {
                    Box(Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                        Text("Nothing to show in this filter", color = Secondary)
                    }
                }
            }
            items(filteredFeed) { activity ->
                ActivityFeedCard(activity)
            }
        }
    }
}

@Composable
private fun RevenueSplitCard(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceBlue).padding(12.dp)
    ) {
        Text(label.uppercase(), color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ActivityFeedCard(item: ActivityFeedItem) {
    val icon = when(item.type) {
        "CHECK-IN" -> Icons.Rounded.HowToReg
        "BOOKING" -> Icons.Rounded.AddBusiness
        "CANCEL" -> Icons.Rounded.Block
        else -> Icons.Rounded.Notifications
    }
    val iconColor = when(item.type) {
        "CHECK-IN" -> Green
        "BOOKING" -> Orange
        "CANCEL" -> Color(0xFFEF4444)
        else -> Secondary
    }

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceBlue.copy(alpha = .6f))
            .border(1.dp, Edge.copy(alpha = .15f), RoundedCornerShape(18.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = .15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.type, color = iconColor, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                Spacer(Modifier.width(6.dp))
                Text("• ${item.time}", color = Secondary, fontSize = 9.sp)
            }
            Text(item.title, color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.detail, color = Secondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        item.amount?.let {
            Text(it, color = White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun OfflineBookingPage(onCreated: (OfflineTicket) -> Unit) {
    val venues = FakeData.turfs.map { it.title }
    var selectedVenue by remember { mutableStateOf(venues.firstOrNull().orEmpty()) }
    var customer by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var createdCode by remember { mutableStateOf<String?>(null) }
    val validEmail = email.contains("@") && email.substringAfter('@').contains('.')

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Offline turf booking", color = White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Create a turf entry for customers booking at the venue.", color = Secondary, fontSize = 12.sp)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                venues.take(3).forEach { venue ->
                    val selected = venue == selectedVenue
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(if (selected) Orange.copy(alpha = .15f) else SurfaceBlue)
                            .border(1.dp, if (selected) Orange else Edge.copy(alpha = .2f), RoundedCornerShape(14.dp))
                            .clickable { selectedVenue = venue }.padding(14.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.SportsSoccer, null, tint = if (selected) Orange else Secondary, modifier = Modifier.size(18.dp))
                        Text(venue, color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp).weight(1f), maxLines = 1)
                        if (selected) Icon(Icons.Rounded.CheckCircle, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        item { ManagerField(customer, { customer = it }, "Customer name") }
        item { ManagerField(email, { email = it.trim() }, "Customer email") }
        item {
            Button(
                onClick = {
                    val code = "EMS-TRF-${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}"
                    createdCode = code
                    onCreated(OfflineTicket(code, customer.trim(), selectedVenue, ManagerArea.TURF))
                },
                enabled = customer.isNotBlank() && validEmail,
                modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange), shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Bolt, null)
                Text("Create turf entry", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            }
        }
        createdCode?.let { code -> item { GeneratedTicket(code) } }
    }
}

@Composable
private fun TicketScannerPage(onScanSuccess: (String) -> Unit) {
    var scanning by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Boolean?>(null) }
    val transition = rememberInfiniteTransition(label = "scan-line")
    val scanPosition by transition.animateFloat(-72f, 72f, infiniteRepeatable(tween(1400), repeatMode = RepeatMode.Reverse), label = "scan-position")
    
    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Scan entry ticket", color = White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Place the ticket QR inside the frame", color = Secondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        item {
            Box(
                Modifier.size(258.dp).clip(RoundedCornerShape(32.dp)).background(Color(0xFF031127))
                    .border(1.dp, Edge.copy(alpha = .5f), RoundedCornerShape(32.dp)).padding(18.dp), contentAlignment = Alignment.Center
            ) {
                Box(Modifier.fillMaxSize().border(2.dp, Orange.copy(alpha = .75f), RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.QrCodeScanner, null, tint = Secondary.copy(alpha = .20f), modifier = Modifier.size(96.dp))
                    if (scanning) Box(Modifier.fillMaxWidth(.86f).height(2.dp).offset { IntOffset(0, scanPosition.roundToInt()) }.background(Orange))
                }
            }
        }
        item {
            Button(
                onClick = { scanning = !scanning }, 
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (scanning) RaisedBlue else Orange), 
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(if(scanning) Icons.Rounded.Stop else Icons.Rounded.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text(if (scanning) "Stop" else "Open Camera", fontWeight = FontWeight.Bold)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(Edge.copy(alpha = .2f)))
                Text("  MANUAL  ", color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Box(Modifier.weight(1f).height(1.dp).background(Edge.copy(alpha = .2f)))
            }
        }
        item { ManagerField(code, { code = it.uppercase(); result = null }, "Ticket ID") }
        item {
            Button(
                onClick = { 
                    result = code.trim().startsWith("EMS-") && code.trim().length >= 10 
                    if (result == true) onScanSuccess(code)
                }, 
                enabled = code.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(16.dp)
            ) { Text("VALIDATE", fontWeight = FontWeight.Black) }
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
            Text("Authorization Active", color = White, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 7.dp))
            Text(code, color = Green, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(top = 7.dp))
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
        Icon(if (valid) Icons.Rounded.CheckCircle else Icons.Rounded.Error, null, tint = color)
        Column(Modifier.padding(start = 11.dp)) {
            Text(if (valid) "Entry Authorized" else "Invalid Token", color = White, fontWeight = FontWeight.Black)
            Text(if (valid) "$code · Checked In" else "Ticket not found", color = Secondary, fontSize = 10.sp)
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

private data class OfflineTicket(val code: String, val customer: String, val venue: String, val area: ManagerArea)
