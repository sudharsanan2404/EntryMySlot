package com.entrymyslot.app.screens.movies

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF080B1A)
private val CardBg = Color(0xFF0D1025)
private val AccentOrange = Color(0xFFFF7A00)
private val White = Color.White
private val Gray = Color(0xFF8A8FA8)

data class MovieCardData(
    val id: Long,
    val title: String,
    val genre: String,
    val rating: String?,
    val posterUrl: String?,
    val language: String?
)

@Composable
fun MoviesScreen(
    movies: List<MovieCardData> = emptyList(),
    onMovieClick: (Long) -> Unit = {},
    onCinemaSelect: (Long) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val genres = listOf("All", "Action", "Comedy", "Drama", "Thriller", "Sci-Fi")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            item {
                MoviesTopBar(onBackClick = onBackClick)
            }
            item {
                MoviesSearchBar()
            }
            item {
                GenreFilterRow(genres)
            }
            item {
                Text(
                    text = "Now Showing",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
            item {
                FeaturedMovieRow(movies.take(5), onMovieClick)
            }
            item {
                Text(
                    text = "All Movies",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(movies) { movie ->
                MovieCardRow(movie = movie, onClick = { onMovieClick(movie.id) })
            }
            if (movies.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No movies found", color = Gray, fontSize = 15.sp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MoviesTopBar(onBackClick: () -> Unit) {
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
            modifier = Modifier
                .size(28.dp)
                .clickable { onBackClick() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Movies",
            color = White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MoviesSearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF12152E))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text("Search movies...", color = Gray, fontSize = 14.sp)
    }
}

@Composable
private fun GenreFilterRow(genres: List<String>) {
    LazyRow(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
    ) {
        items(genres) { g ->
            val selected = g == "All"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) AccentOrange else Color(0xFF12152E))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(g, color = if (selected) White else Gray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun FeaturedMovieRow(movies: List<MovieCardData>, onMovieClick: (Long) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
    ) {
        items(movies) { m ->
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A1E3A))
                    .clickable { onMovieClick(m.id) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Movie, contentDescription = null, tint = AccentOrange.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(m.title, color = White, fontSize = 12.sp, maxLines = 2, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
                    if (m.rating != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Star, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(m.rating, color = AccentOrange, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieCardRow(movie: MovieCardData, onClick: () -> Unit) {
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
                .size(width = 60.dp, height = 80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1E3A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Movie, contentDescription = null, tint = AccentOrange.copy(alpha = 0.4f), modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(movie.title, color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(3.dp))
            Text("${movie.genre}${movie.language?.let { " • $it" } ?: ""}", color = Gray, fontSize = 12.sp)
        }
        if (movie.rating != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Star, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(movie.rating, color = AccentOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
