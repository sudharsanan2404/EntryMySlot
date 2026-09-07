package com.entrymyslot.app.screens.movies
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.R
import com.entrymyslot.app.core.components.PremiumLoadingState
import com.entrymyslot.app.core.components.PremiumErrorState
import com.entrymyslot.app.core.components.PremiumEmptyState
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.data.model.Movie

private val MovieOrange = Color(0xFFFF5400)
private val MovieBackground = Color(0xFF001329)
private val MovieSecondary = Color(0xFFABA9DF)

@Composable
fun MovieDetailsScreen(movieId: String, onBackClick: () -> Unit = {}, onBookClick: () -> Unit = {}) {
    val movieViewModel: MovieViewModel = viewModel(key = "movie_details_$movieId")
    val state by movieViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(movieId) { movieViewModel.loadMovie(movieId) }
    Column(Modifier.fillMaxSize().background(MovieBackground).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Text("Movie overview", color = Color.White, fontSize = 20.sp)
        }
        val movie = state.movie
        if (movie != null) {
            Box(Modifier.weight(1f)) {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 90.dp)) {
                    item("hero") { MovieHero(movie) }
                    item("information") { MovieInformation(movie) }
                    if (!movie.trailerUrl.isNullOrBlank()) {
                        item("trailer") { MovieTrailer(movie) }
                    }
                    if (movie.castNames.isNotEmpty() || !movie.director.isNullOrBlank()) {
                        item("cast_heading") { MovieHeading("Cast & crew", Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) }
                        item("cast") { MovieCast(movie) }
                    }
                }
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(MovieBackground)
                    .navigationBarsPadding().padding(horizontal = 18.dp, vertical = 8.dp)) {
                    Button(onClick = onBookClick, modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(7.dp), colors = ButtonDefaults.buttonColors(containerColor = MovieOrange)) {
                        Text("Book Tickets", color = Color.White, fontSize = 18.sp)
                        Spacer(Modifier.width(18.dp))
                        Icon(Icons.Rounded.ArrowForward, null, tint = Color.White)
                    }
                }
            }
        } else if (state.isLoading) {
            PremiumLoadingState(modifier = Modifier.fillMaxSize(), message = "Loading movie details...")
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PremiumErrorState(title = "Movie Load Failed", message = state.errorMessage ?: "Movie details are unavailable.", onRetry = movieViewModel::retry)
            }
        }
    }
}

@Composable
private fun MovieHero(movie: Movie) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = maxWidth
        val posterWidth = (width * .32f).coerceAtMost(180.dp)
        Box(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(width * .60f)) {
                AsyncImage(model = movie.bannerUrl ?: movie.imageUrl, contentDescription = null,
                    placeholder = painterResource(R.drawable.movie_poster_fallback), error = painterResource(R.drawable.movie_poster_fallback),
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    0f to Color.Transparent, .45f to Color.Transparent, 1f to MovieBackground)))
            }
            Row(Modifier.fillMaxWidth().padding(start = 26.dp, end = 24.dp, top = width * .29f, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AsyncImage(model = movie.imageUrl, contentDescription = "${movie.title} poster",
                    placeholder = painterResource(R.drawable.movie_poster_fallback), error = painterResource(R.drawable.movie_poster_fallback),
                    contentScale = ContentScale.Crop, modifier = Modifier.width(posterWidth).aspectRatio(.73f)
                        .clip(RoundedCornerShape(8.dp)).border(1.dp, MovieSecondary.copy(alpha = .65f), RoundedCornerShape(8.dp)))
                Column(Modifier.weight(1f)) {
                    Text(movie.title, color = Color.White, fontSize = 32.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(movie.genre.replace(", ", " • "), color = MovieSecondary, fontSize = 16.sp)
                    movie.rating?.let { rating ->
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, tint = MovieOrange, modifier = Modifier.size(22.dp))
                            Text(" $rating", color = MovieOrange, fontSize = 19.sp)
                            Text(" /10", color = MovieSecondary, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieInformation(movie: Movie) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 25.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(Icons.Rounded.Schedule, null, tint = MovieSecondary, modifier = Modifier.size(21.dp))
            Text(movie.duration, color = Color.White, fontSize = 15.sp)
            movie.censorRating?.takeIf(String::isNotBlank)?.let {
                Text(" • ", color = MovieSecondary)
                Text(it, color = Color.White, fontSize = 15.sp)
                Text("Certified", color = MovieSecondary, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Language, null, tint = MovieSecondary, modifier = Modifier.size(21.dp))
            Text("  Languages", color = MovieSecondary, fontSize = 14.sp)
        }
        Text(movie.language.replace(", ", "  •  "), color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(top = 9.dp))
        if (movie.releaseDate.isNotBlank()) {
            val release = remember(movie.releaseDate) {
                runCatching { java.time.LocalDate.parse(movie.releaseDate.substringBefore('T')).format(
                    java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH)) }.getOrDefault(movie.releaseDate.substringBefore('T'))
            }
            Row(Modifier.padding(top = 27.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarToday, null, tint = MovieSecondary, modifier = Modifier.size(20.dp))
                Text("  Released $release", color = MovieSecondary, fontSize = 14.sp)
            }
        }
        if (movie.description.isNotBlank()) {
            MovieHeading("About the movie", Modifier.padding(top = 4.dp, bottom = 6.dp))
            Text(movie.description, color = MovieSecondary, fontSize = 14.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun MovieHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.semantics { heading() }, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun MovieTrailer(movie: Movie) {
    val uriHandler = LocalUriHandler.current
    Column(Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
        MovieHeading("Trailer")
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().aspectRatio(2.65f).clip(RoundedCornerShape(9.dp))
            .border(1.dp, MovieSecondary.copy(alpha = .65f), RoundedCornerShape(9.dp))
            .clickable(role = Role.Button, onClickLabel = "Play official trailer") {
                movie.trailerUrl?.let { runCatching { uriHandler.openUri(it) } }
            }, contentAlignment = Alignment.Center) {
            AsyncImage(model = movie.bannerUrl ?: movie.imageUrl, contentDescription = null,
                placeholder = painterResource(R.drawable.movie_poster_fallback), error = painterResource(R.drawable.movie_poster_fallback),
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .5f)))))
            Box(Modifier.size(48.dp).clip(CircleShape).background(MovieOrange), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text("Official trailer", color = Color.White, fontSize = 13.sp, modifier = Modifier.align(Alignment.BottomStart).padding(9.dp))
        }
    }
}

@Composable
private fun MovieCast(movie: Movie) {
    val people = movie.castNames.map { it to "Actor" } + listOfNotNull(movie.director?.takeIf(String::isNotBlank)?.let { it to "Director" })
    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(people.size) { index ->
            val (name, role) = people[index]
            Column(Modifier.width(84.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF032044))
                    .border(1.dp, MovieSecondary.copy(alpha = .5f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
                        color = MovieSecondary, fontSize = 23.sp, fontWeight = FontWeight.Medium)
                }
                Text(name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                Text(role, color = MovieSecondary, fontSize = 11.sp)
            }
        }
    }
}
