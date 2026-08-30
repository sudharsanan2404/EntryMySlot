package com.entrymyslot.app.screens.turf

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.model.SportsVenueDto

private val TurfBlueTop = Color(0xFF063DB5)
private val TurfBlueBottom = Color(0xFF041F5D)
private val TurfOrange = Color(0xFFFF8A00)
private val TurfWhite = Color.White
private val TurfGray = Color(0xFFB8C0D0)
private val TurfCard = Color(0xFF111D32)

@Composable
fun TurfScreen(
    venues: List<SportsVenueDto> = emptyList(),
    isLoading: Boolean = false,
    error: String? = null,
    onBackClick: () -> Unit = {},
    onVenueClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredVenues = remember(venues, searchQuery) {
        if (searchQuery.isBlank()) venues
        else venues.filter {
            it.name.contains(searchQuery, true) ||
            (it.city?.contains(searchQuery, true) == true) ||
            it.venueType.contains(searchQuery, true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TurfBlueTop, Color(0xFF0737A4), Color(0xFF062E88), TurfBlueBottom)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
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
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TurfWhite,
                        modifier = Modifier.size(28.dp).clickable { onBackClick() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Sports Venues", color = TurfWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF12152E))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = TurfGray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Search venues...", color = TurfGray, fontSize = 14.sp)
                }
            }

            if (error != null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(text = error, color = Color(0xFFFF5252), fontSize = 13.sp)
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TurfOrange)
                    }
                }
            } else {
                items(filteredVenues) { venue ->
                    TurfVenueCard(
                        venue = venue,
                        onClick = { onVenueClick(venue.id.toString()) }
                    )
                }
                if (filteredVenues.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                            Text("No venues found", color = TurfGray, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TurfVenueCard(venue: SportsVenueDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TurfCard)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF172B4A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SportsSoccer,
                contentDescription = null,
                tint = TurfOrange.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = venue.name,
                color = TurfWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${venue.venueType}${venue.city?.let { " • $it" } ?: ""}",
                color = TurfGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Star, contentDescription = null, tint = TurfOrange, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "4.5", color = TurfGray, fontSize = 12.sp)
                if (venue.description?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = venue.description!!.take(30) + if (venue.description!!.length > 30) "..." else "",
                        color = TurfGray.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
