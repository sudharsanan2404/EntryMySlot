package com.entrymyslot.app.screens.movies

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.R
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.screens.home.PopularEvent

private val MovieOrange = Color(0xFFFA580B)
private val MovieBackground = Color(0xFF061A38)
private val MovieBlue = Color(0xFF0A2D62)
private val MovieBlueRaised = Color(0xFF0B274F)
private val MovieBlueEdge = Color(0xFF3976A8)
private val MovieWhite = Color(0xFFF8FAFF)
private val MovieSecondary = Color(0xFFA8B8CF)

@Composable
fun MoviesListScreen(
    movies: List<PopularEvent>,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onMovieClick: (PopularEvent) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            MoviesTopBar(onBackClick = onBackClick, onSearchClick = onSearchClick)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = movies,
                    key = { movie -> movie.id }
                ) { movie ->
                    VerticalMovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MoviesTopBar(onBackClick: () -> Unit, onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = MovieWhite,
            modifier = Modifier.size(40.dp).padding(9.dp).clickable(onClick = onBackClick)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Movies",
            color = MovieWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search movies",
            tint = MovieOrange,
            modifier = Modifier.size(40.dp).padding(9.dp).clickable(onClick = onSearchClick)
        )
    }
}

@Composable
fun VerticalMovieCard(
    movie: PopularEvent,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(18.dp)
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(110),
        label = "movieCardScale"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 7.dp,
        animationSpec = tween(120),
        label = "movieCardElevation"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) {
            MovieBlueEdge.copy(alpha = 0.42f)
        } else {
            MovieBlueEdge.copy(alpha = 0.25f)
        },
        animationSpec = tween(120),
        label = "movieCardBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .shadow(
                elevation = cardElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.24f)
            )
            .clip(shape)
            .background(MovieBlueRaised.copy(alpha = 0.98f))
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Open ${movie.title}",
                onClick = onClick
            )
            .semantics { role = Role.Button }
    ) {
        MoviePoster(movie = movie)
        MovieCardDetails(movie = movie)
    }
}

@Composable
private fun MoviePoster(movie: PopularEvent) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .background(MovieBlue)
    ) {
        coil3.compose.AsyncImage(
            model = movie.imageUrl ?: R.drawable.movie_poster_fallback,
            contentDescription = movie.title,
            placeholder = painterResource(R.drawable.movie_poster_fallback),
            error = painterResource(R.drawable.movie_poster_fallback),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MovieBackground.copy(alpha = 0.3f)
                        )
                    )
                )
        )

    }
}

@Composable
private fun MoviePosterFallback(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MovieBlue.copy(alpha = 0.98f),
                        MovieBlueRaised
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MovieBlueEdge.copy(alpha = 0.14f))
                    .border(
                        1.dp,
                        MovieBlueEdge.copy(alpha = 0.24f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = MovieWhite.copy(alpha = 0.7f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "MOVIE",
                color = MovieSecondary.copy(alpha = 0.72f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
                modifier = Modifier.semantics {
                    contentDescription = "No poster available for $title"
                }
            )
        }
    }
}

@Composable
private fun MovieCardDetails(movie: PopularEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = movie.title,
            color = MovieWhite,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.heightIn(min = 38.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = movie.date,
            color = MovieSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(MovieOrange.copy(alpha = 0.1f))
                .border(
                    1.dp,
                    MovieOrange.copy(alpha = 0.2f),
                    RoundedCornerShape(9.dp)
                )
                .padding(horizontal = 9.dp, vertical = 6.dp)
        ) {
            Text(
                text = movie.price,
                color = MovieOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
