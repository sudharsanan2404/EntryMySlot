package com.entrymyslot.app.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.entrymyslot.app.R
import com.entrymyslot.app.screens.home.PopularEvent
import com.entrymyslot.app.screens.home.GlowBackground

@Composable
fun MovieDetailsScreen(movie: PopularEvent, onBackClick: () -> Unit, onBookClick: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        GlowBackground()
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 104.dp)) {
            item { MovieHero(movie, onBackClick) }
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                    Text("About the Movie", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A spectacular big-screen experience packed with action, heart and unforgettable moments. Reserve the best seats at a cinema near you.",
                        color = Color(0xFF98A2B3), fontSize = 14.sp, lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Text("Cast", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(listOf("Lead Actor", "Lead Actress", "Director", "Producer"), key = { it }) { CastMember(it) }
                }
            }
            item {
                Column(Modifier.padding(20.dp)) {
                    Text("Trailer", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier.fillMaxWidth().height(172.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFFF8A3D).copy(alpha = .10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PlayCircle, "Play trailer", tint = Color(0xFFFF8A3D), modifier = Modifier.size(58.dp))
                    }
                }
            }
        }

        Button(
            onClick = onBookClick,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp).height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A3D))
        ) { Text("Book Tickets", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable
private fun MovieHero(movie: PopularEvent, onBackClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(410.dp)) {
        AsyncImage(
            model = movie.imageUrl ?: R.drawable.entrymyslot,
            contentDescription = "${movie.title} poster",
            placeholder = painterResource(R.drawable.entrymyslot),
            error = painterResource(R.drawable.entrymyslot),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .12f), Color(0xFF061A33)), startY = 80f)))
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.statusBarsPadding().padding(8.dp).background(Color.Black.copy(alpha = .42f), CircleShape)
        ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(movie.title, color = Color.White, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, null, tint = Color(0xFFFF8A3D), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("8.5/10", color = Color(0xFFFF8A3D), fontWeight = FontWeight.Bold)
                Text("  •  Tamil  •  Action  •  2h 35m", color = Color.White.copy(alpha = .78f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CastMember(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(Modifier.size(62.dp).background(Color(0xFF1648D5).copy(alpha = .22f), CircleShape), contentAlignment = Alignment.Center) {
            Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(Modifier.height(7.dp))
        Text(name, color = Color(0xFFB8C0D0), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
