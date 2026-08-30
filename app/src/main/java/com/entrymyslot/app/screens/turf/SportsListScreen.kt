package com.entrymyslot.app.screens.turf

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.PopularEvent
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.core.components.ElevatedCardSubtitle
import com.entrymyslot.app.core.components.ElevatedCardTitle
import com.entrymyslot.app.core.components.ElevatedContrastCard
import com.entrymyslot.app.core.components.EntryCardAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsListScreen(
    sports: List<PopularEvent>,
    onBackClick: () -> Unit,
    onSportClick: (PopularEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TopAppBar(
                title = { Text("Sports", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sports, key = { it.id }) { sport ->
                    SportListItem(
                        sport = sport,
                        onClick = { onSportClick(sport) }
                    )
                }
            }
        }
    }
}

@Composable
fun SportListItem(
    sport: PopularEvent,
    onClick: () -> Unit
) {
    ElevatedContrastCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1648D5).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                if (sport.imageUrl != null) {
                    coil3.compose.AsyncImage(
                        model = sport.imageUrl,
                        contentDescription = sport.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("TURF", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                ElevatedCardTitle(sport.title)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = EntryCardAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    ElevatedCardSubtitle(sport.location, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Starting from", color = Color(0xFF98A2B3), fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = sport.price.split("/").first().trim(), color = EntryCardAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = EntryCardAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("4.5", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
