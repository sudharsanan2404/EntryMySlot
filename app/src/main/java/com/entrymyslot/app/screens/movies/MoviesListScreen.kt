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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onMovieClick: (PopularEvent) -> Unit
) {
    var favoriteMovies by remember { mutableStateOf(setOf<String>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B4DA5).copy(alpha = 0.38f),
                            MovieBackground.copy(alpha = 0f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            MoviesTopBar(onBackClick = onBackClick)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = 28.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = movies,
                    key = { movie -> movie.id }
                ) { movie ->
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
private fun MoviesTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MovieBlue.copy(alpha = 0.76f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MovieWhite,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Movies",
            color = MovieWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
fun VerticalMovieCard(
    movie: PopularEvent,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
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
        MoviePoster(
            movie = movie,
            isFavorite = isFavorite,
            onFavoriteClick = onFavoriteClick
        )
        MovieCardDetails(movie = movie)
    }
}

@Composable
private fun MoviePoster(
    movie: PopularEvent,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .background(MovieBlue)
    ) {
        if (movie.imageUrl != null) {
            coil3.compose.AsyncImage(
                model = movie.imageUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            MoviePosterFallback(title = movie.title)
        }

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

        FavoriteButton(
            isFavorite = isFavorite,
            movieTitle = movie.title,
            onClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
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
private fun FavoriteButton(
    isFavorite: Boolean,
    movieTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(90),
        label = "favoriteButtonScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isFavorite) {
            MovieOrange.copy(alpha = 0.2f)
        } else {
            MovieBackground.copy(alpha = 0.76f)
        },
        animationSpec = tween(150),
        label = "favoriteContainer"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (isFavorite) {
                    MovieOrange.copy(alpha = 0.44f)
                } else {
                    Color.White.copy(alpha = 0.16f)
                },
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = if (isFavorite) {
                    "Remove $movieTitle from favorites"
                } else {
                    "Add $movieTitle to favorites"
                },
                onClick = onClick
            )
            .semantics {
                contentDescription = if (isFavorite) {
                    "Remove $movieTitle from favorites"
                } else {
                    "Add $movieTitle to favorites"
                }
                stateDescription = if (isFavorite) "Favorited" else "Not favorited"
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = {
                (fadeIn(tween(120)) + scaleIn(tween(140), initialScale = 0.65f))
                    .togetherWith(
                        fadeOut(tween(90)) + scaleOut(tween(100), targetScale = 0.72f)
                    )
            },
            label = "favoriteIcon"
        ) { favorite ->
            Icon(
                imageVector = if (favorite) {
                    Icons.Rounded.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                contentDescription = null,
                tint = if (favorite) MovieOrange else MovieWhite,
                modifier = Modifier.size(19.dp)
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
