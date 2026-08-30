package com.entrymyslot.app.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.PopularEvent
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.core.components.EntryCardAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsListScreen(
    title: String = "Events",
    events: List<PopularEvent>,
    onBackClick: () -> Unit,
    onEventClick: (PopularEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TopAppBar(
                title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(events, key = { it.id }) { event ->
                    EventListItem(
                        event = event,
                        onClick = { onEventClick(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventListItem(
    event: PopularEvent,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = .35f), spotColor = Color.Black.copy(alpha = .35f))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1D2550), Color(0xFF171E42))))
            .border(1.dp, Color.White.copy(alpha = .06f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            if (event.imageUrl != null) {
                coil3.compose.AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF1648D5).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("EVENT", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(text = event.price, color = EntryCardAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarToday, null, tint = EntryCardAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = event.date, color = Color(0xFF9AA3C7), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, tint = Color(0xFF98A2B3), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = event.location, color = Color(0xFF9AA3C7), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
