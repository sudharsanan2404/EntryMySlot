package com.entrymyslot.app.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.PopularEvent

@Composable
fun MovieOverviewContent(
    movie: PopularEvent,
    onBookClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Movie Poster Placeholder
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "POSTER", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tamil • Action • 2h 35m",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFF8A00),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "8.5",
                        color = Color(0xFFFF8A00),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "About the Movie",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Experience this epic blockbuster on the big screen. A thrilling journey filled with intense action and emotion. Now showing in cinemas near you.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // CAST
        Text(
            text = "Cast & Crew",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(listOf("Actor 1", "Actor 2", "Director", "Producer")) { person ->
                CastItem(name = person)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBookClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
        ) {
            Text(text = "Book Tickets", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun CastItem(name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(62.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF263653)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👤", fontSize = 23.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = name, color = Color(0xFFB8C0D0), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
