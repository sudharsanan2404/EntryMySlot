package com.entrymyslot.app.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.entrymyslot.app.EntryMySlotApp

private val BgDark = Color(0xFF080B1A)
private val CardBg = Color(0xFF0D1025)
private val AccentOrange = Color(0xFFFF7A00)
private val White = Color.White
private val Gray = Color(0xFF8A8FA8)

sealed class BookingTab(val title: String) {
    object All : BookingTab("All")
    object Events : BookingTab("Events")
    object Movies : BookingTab("Movies")
    object Turfs : BookingTab("Turfs")
}

@Composable
fun BookingScreen(
    @Suppress("UNUSED_PARAMETER") selectedBookingId: String? = null,
    onTicketClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val viewModel = remember { BookingViewModel(EntryMySlotApp.instance.appContainer.bookingRepository) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf<BookingTab>(BookingTab.All) }

    val allBookings = buildList {
        addAll(uiState.eventBookings.map { it.copy(type = "Event") })
        addAll(uiState.movieBookings.map { it.copy(type = "Movie") })
        addAll(uiState.turfBookings.map { it.copy(type = "Turf") })
    }.sortedByDescending { it.date }

    val filtered = when (selectedTab) {
        is BookingTab.All -> allBookings
        is BookingTab.Events -> allBookings.filter { it.type == "Event" }
        is BookingTab.Movies -> allBookings.filter { it.type == "Movie" }
        is BookingTab.Turfs -> allBookings.filter { it.type == "Turf" }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = White,
                        modifier = Modifier.size(28.dp).clickable { onBackClick() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("My Bookings", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                BookingTabsRow(
                    tabs = listOf(BookingTab.All, BookingTab.Events, BookingTab.Movies, BookingTab.Turfs),
                    selected = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentOrange)
                    }
                }
            } else {
                items(filtered) { booking ->
                    BookingCard(booking = booking, onClick = { onTicketClick(booking.id) })
                }
                if (filtered.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No bookings yet", color = Gray, fontSize = 15.sp)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun BookingTabsRow(
    tabs: List<BookingTab>,
    selected: BookingTab,
    onSelect: (BookingTab) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs) { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AccentOrange else Color(0xFF12152E))
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = tab.title,
                    color = if (isSelected) White else Gray,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun BookingCard(booking: BookingItem, onClick: () -> Unit) {
    val icon = when (booking.type) {
        "Event" -> Icons.Outlined.Event
        "Movie" -> Icons.Outlined.Movie
        "Turf" -> Icons.Outlined.SportsSoccer
        else -> Icons.Outlined.ConfirmationNumber
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A1E3A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(booking.title, color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text(booking.date, color = Gray, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = booking.status.replaceFirstChar { it.uppercase() },
                color = when (booking.status.lowercase()) {
                    "confirmed" -> Color(0xFF4CAF50)
                    "cancelled" -> Color(0xFFFF5252)
                    "pending" -> AccentOrange
                    else -> Gray
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(booking.amount, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class BookingItem(
    val id: String,
    val type: String,
    val title: String,
    val date: String,
    val status: String,
    val amount: String
)
