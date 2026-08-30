package com.entrymyslot.app.screens.booking

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Home
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.core.components.ElevatedCardSubtitle
import com.entrymyslot.app.core.components.ElevatedCardTitle
import com.entrymyslot.app.core.components.ElevatedContrastCard
import com.entrymyslot.app.core.components.EntryCardAccent

// ------------------------------------------------------------
// COLORS & STYLES
// ------------------------------------------------------------
private val EntryBlueTop = Color(0xFF0B3A82)
private val EntryBlueBottom = Color(0xFF061A33)
private val EntryOrange = EntryCardAccent
private val EntryWhite = Color.White
private val EntryGray = Color(0xFF98A2B3)
private val EntryCardBg = Color(0xFF0E0B38).copy(alpha = .68f)
private val EntryBorder = Color(0xFF1648D5).copy(alpha = .38f)
private val StatusUpcoming = EntryCardAccent
private val StatusCompleted = Color(0xFF4CAF50)
private val StatusCancelled = Color(0xFFE53935)

// ------------------------------------------------------------
// MODELS
// ------------------------------------------------------------
enum class BookingType { MOVIE, TURF, EVENT, CONCERT }
enum class BookingStatus { UPCOMING, COMPLETED, CANCELLED }

data class BookingItem(
    val id: String,
    val type: BookingType,
    val title: String,
    val location: String,
    val dateTime: String,
    val details: String,
    val price: String,
    val status: BookingStatus
)

// ------------------------------------------------------------
// SAMPLE DATA
// ------------------------------------------------------------
val upcomingBookings = listOf(
    BookingItem("1", BookingType.MOVIE, "The Epic Blockbuster", "PVR Cinemas", "28 Aug 2026 • 1:30 PM", "Seats: A3, A4", "₹360", BookingStatus.UPCOMING),
    BookingItem("2", BookingType.TURF, "Green Arena Turf", "Chennai", "29 Aug 2026 • 6:00 PM - 8:00 PM", "2 Hours Booked", "₹1,600", BookingStatus.UPCOMING),
    BookingItem("3", BookingType.EVENT, "Live Cricket Championship", "Nehru Stadium", "30 Aug 2026 • 6:30 PM", "VIP × 2 • Gold × 2", "₹7,400", BookingStatus.UPCOMING)
)

val pastBookings = listOf(
    BookingItem("4", BookingType.MOVIE, "The Dark Knight", "Luxe Cinemas", "25 Aug 2026 • 7:30 PM", "Seats: B4, B5", "₹360", BookingStatus.COMPLETED),
    BookingItem("5", BookingType.TURF, "Power Turf", "Adyar", "20 Aug 2026 • 5:00 PM", "1 Hour Booked", "₹800", BookingStatus.CANCELLED)
)

// ------------------------------------------------------------
// BOOKING SCREEN
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onBackClick: () -> Unit = {},
    onBottomNavigationClick: (String) -> Unit = {},
    onViewTicketClick: (BookingItem) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past")
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Movies", "Turf", "Events", "Concerts")

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            TopAppBar(
                title = { Text("My Bookings", color = EntryWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EntryWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EntryCardBg)
                    .padding(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EntryOrange else Color.Transparent)
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) EntryWhite else EntryGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Filters (Only for Bookings tabs)
            if (selectedTab < 2) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters, key = { it }) { filter ->
                        FilterChip(
                            label = filter,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }

            // List Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Upcoming
                        val filtered = filterBookings(upcomingBookings, selectedFilter)
                        if (filtered.isEmpty()) {
                            item { EmptyState("No Upcoming Bookings", "Your next experience is waiting for you.") }
                        } else {
                            items(filtered, key = { it.id }) { BookingCard(it, onViewTicketClick) }
                        }
                    }
                    1 -> { // Past
                        val filtered = filterBookings(pastBookings, selectedFilter)
                        if (filtered.isEmpty()) {
                            item { EmptyState("No Past Bookings", "Go book something amazing!") }
                        } else {
                            items(filtered, key = { it.id }) { BookingCard(it, onViewTicketClick) }
                        }
                    }
                }
            }

            // Bottom Navigation
            BookingBottomNavigation(
                selectedItem = "My Bookings",
                onItemSelected = onBottomNavigationClick
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) EntryOrange.copy(alpha = 0.2f) else EntryCardBg,
        border = BorderStroke(1.dp, if (isSelected) EntryOrange else EntryBorder)
    ) {
        Text(
            text = label,
            color = if (isSelected) EntryOrange else EntryWhite,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BookingCard(item: BookingItem, onViewTicketClick: (BookingItem) -> Unit) {
    ElevatedContrastCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Type & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(item.type) {
                            BookingType.MOVIE -> Icons.Outlined.Movie
                            BookingType.TURF -> Icons.Outlined.SportsSoccer
                            BookingType.EVENT -> Icons.Outlined.ConfirmationNumber
                            BookingType.CONCERT -> Icons.Outlined.MusicNote
                        },
                        contentDescription = null,
                        tint = EntryGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.type.name,
                        color = EntryGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                StatusBadge(item.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            ElevatedCardTitle(item.title)
            Spacer(modifier = Modifier.height(2.dp))
            ElevatedCardSubtitle(item.location)

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, null, tint = EntryOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.dateTime, color = EntryWhite.copy(alpha = 0.9f), fontSize = 13.sp)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, null, tint = EntryGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.details, color = EntryGray, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.price, color = EntryWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                
                Button(
                    onClick = { onViewTicketClick(item) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EntryOrange),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        if (item.type == BookingType.TURF) "View Booking" else "View Ticket",
                        color = EntryWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: BookingStatus) {
    val (color, text) = when(status) {
        BookingStatus.UPCOMING -> StatusUpcoming to "UPCOMING"
        BookingStatus.COMPLETED -> StatusCompleted to "COMPLETED"
        BookingStatus.CANCELLED -> StatusCancelled to "CANCELLED"
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}


@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.SentimentDissatisfied,
            null,
            tint = EntryGray.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, color = EntryWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = EntryGray, fontSize = 14.sp, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        listOf("Movies", "Turf", "Events").forEach { category ->
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                border = BorderStroke(1.dp, EntryBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EntryOrange)
            ) {
                Text("Explore $category")
            }
        }
    }
}

private fun filterBookings(list: List<BookingItem>, filter: String): List<BookingItem> {
    if (filter == "All") return list
    val type = when(filter) {
        "Movies" -> BookingType.MOVIE
        "Turf" -> BookingType.TURF
        "Events" -> BookingType.EVENT
        "Concerts" -> BookingType.CONCERT
        else -> null
    }
    return list.filter { it.type == type }
}

@Composable
private fun BookingBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val items = listOf(
        Triple("Home", Icons.Outlined.Home, Icons.Rounded.Home),
        Triple("Search", Icons.Outlined.Search, Icons.Outlined.Search),
        Triple("My Bookings", Icons.Outlined.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        Triple("Profile", Icons.Outlined.AccountCircle, Icons.Outlined.AccountCircle)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0E0B38).copy(alpha = .86f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color(0xFF1648D5).copy(alpha = .38f)),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    val selected = selectedItem == item.first
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) EntryOrange.copy(alpha = .16f) else Color.Transparent)
                            .clickable { onItemSelected(item.first) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selected) item.third else item.second,
                            contentDescription = item.first,
                            tint = if (selected) EntryOrange else EntryGray,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.first,
                            color = if (selected) EntryOrange else EntryGray,
                            fontSize = 8.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
