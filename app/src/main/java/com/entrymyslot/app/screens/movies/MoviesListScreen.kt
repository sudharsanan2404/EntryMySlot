package com.entrymyslot.app.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.screens.home.PopularEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesListScreen(
    movies: List<PopularEvent>,
    onBackClick: () -> Unit,
    onMovieClick: (PopularEvent) -> Unit
) {
    var favoriteMovies by remember { mutableStateOf(setOf<String>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TopAppBar(
                title = { Text("Movies", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(movies) { movie ->
                    VerticalMovieCard(
                        movie = movie,
                        isFavorite = favoriteMovies.contains(movie.id),
                        onFavoriteClick = {
                            favoriteMovies = if (favoriteMovies.contains(movie.id)) {
                                favoriteMovies - movie.id
                            } else {
                                favoriteMovies + movie.id
                            }
                        },
                        onClick = { onMovieClick(movie) }
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalMovieCard(
    movie: PopularEvent,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A1D4D))
            .border(1.dp, Color(0xFF1E3A8A).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            if (movie.imageUrl != null) {
                coil3.compose.AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF1E3A8A).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MOVIE", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = movie.date, color = Color(0xFF98A2B3), fontSize = 11.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = movie.price, color = Color(0xFFFF8A00), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
