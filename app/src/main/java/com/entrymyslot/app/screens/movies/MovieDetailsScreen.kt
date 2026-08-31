package com.entrymyslot.app.screens.movies

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.entrymyslot.app.R
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.BookingType
import com.entrymyslot.app.data.model.Movie

private val MovieOrange = Color(0xFFFA580B)
private val MovieBackground = Color(0xFF061A38)
private val MovieBlue = Color(0xFF0A2D62)
private val MovieBlueRaised = Color(0xFF0B274F)
private val MovieBlueEdge = Color(0xFF3976A8)
private val MovieWhite = Color(0xFFF8FAFF)
private val MovieSecondary = Color(0xFFA8B8CF)
private val MovieDivider = Color(0xFF24476F)

@Composable
fun MovieDetailsScreen(
    movie: Movie,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 112.dp)
        ) {
            item(key = "movie_hero") {
                MovieHero(movie = movie, onBackClick = onBackClick)
            }

            item(key = "about") {
                AboutMovieSection(movie)
            }

            item(key = "interest") {
                MovieInterestCard(movie)
            }

            item(key = "cast_heading") {
                SectionHeading(
                    text = "Cast",
                    modifier = Modifier.padding(start = 20.dp, top = 26.dp, end = 20.dp)
                )
                Spacer(modifier = Modifier.height(13.dp))
            }

            item(key = "cast") {
                CastRow(movie)
            }

            item(key = "trailer") {
                TrailerSection()
            }
        }

        BookingBottomBar(
            onBookClick = onBookClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MovieHero(
    movie: Movie,
    onBackClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val heroHeight = (maxWidth * 1.02f).coerceIn(350.dp, 420.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
        ) {
            AsyncImage(
                model = movie.imageUrl ?: R.drawable.movie_poster_fallback,
                contentDescription = "${movie.title} poster",
                placeholder = painterResource(R.drawable.movie_poster_fallback),
                error = painterResource(R.drawable.movie_poster_fallback),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Black.copy(alpha = 0.16f),
                                0.43f to Color.Transparent,
                                0.72f to MovieBackground.copy(alpha = 0.62f),
                                1f to MovieBackground
                            )
                        )
                    )
            )

            PremiumBackButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 12.dp, top = 8.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Text(
                    text = movie.title,
                    color = MovieWhite,
                    fontSize = 29.sp,
                    lineHeight = 33.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                MovieMetadata(movie)
            }
        }
    }
}

@Composable
private fun PremiumBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "backButtonScale"
    )

    Box(
        modifier = modifier
            .size(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Back",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = MovieWhite,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun MovieInterestCard(movie: Movie) {
    val interested = FakeData.isWishlisted(movie.id)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MovieBlueRaised)
            .border(1.dp, MovieBlueEdge.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Interested in this movie?", color = MovieWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                "Get updates when showtimes or booking information changes.",
                color = MovieSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 2.dp, end = 6.dp)
            )
        }
        Button(
            onClick = { FakeData.toggleWishlist(movie.id, BookingType.MOVIE) },
            modifier = Modifier.height(34.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (interested) MovieBlue else MovieOrange
            )
        ) { Text(if (interested) "Interested ✓" else "Interested", color = MovieWhite, fontSize = 10.sp) }
    }
}

@Composable
private fun MovieMetadata(movie: Movie) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MovieOrange,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "${movie.rating}/10",
                color = MovieOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        MetadataDot()
        MetadataText(text = movie.language)
        MetadataDot()
        MetadataText(text = movie.genre)
        MetadataDot()
        MetadataText(text = movie.duration)
    }
}

@Composable
private fun MetadataDot() {
    Box(
        modifier = Modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(MovieSecondary.copy(alpha = 0.66f))
    )
}

@Composable
private fun MetadataText(text: String) {
    Text(
        text = text,
        color = MovieWhite.copy(alpha = 0.8f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1
    )
}

@Composable
private fun AboutMovieSection(movie: Movie) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        SectionHeading(text = "About the Movie")
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = movie.description,
            color = MovieSecondary,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun SectionHeading(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.semantics { heading() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(50))
                .background(MovieOrange)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = text,
            color = MovieWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
private fun CastRow(movie: Movie) {
    val cast = FakeData.getCast(movie)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items = cast, key = { member -> member.id }) { member ->
            CastMemberCard(name = member.name, imageUrl = member.imageUrl)
        }
    }
}

@Composable
private fun CastMemberCard(name: String, imageUrl: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .semantics {
                contentDescription = "$name cast photo"
            }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MovieBlue.copy(alpha = 0.96f),
                            MovieBlueRaised
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = MovieOrange.copy(alpha = 0.48f),
                    shape = CircleShape
                ),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$name photo",
                placeholder = painterResource(R.drawable.profile_avatar_fallback),
                error = painterResource(R.drawable.profile_avatar_fallback),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = name,
            color = MovieSecondary,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrailerSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        SectionHeading(text = "Trailer")
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MovieBlueRaised,
                            MovieBlue.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = MovieBlueEdge.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = MovieOrange.copy(alpha = 0.24f),
                        spotColor = MovieOrange.copy(alpha = 0.26f)
                    )
                    .clip(CircleShape)
                    .background(MovieOrange)
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play trailer",
                    tint = MovieWhite,
                    modifier = Modifier.size(29.dp)
                )
            }
        }
    }
}

@Composable
private fun BookingBottomBar(
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(100),
        label = "bookButtonScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 6.dp,
        animationSpec = tween(110),
        label = "bookButtonElevation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF071A35).copy(alpha = 0.98f))
            .drawBehind {
                drawLine(
                    color = MovieDivider.copy(alpha = 0.72f),
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Button(
            onClick = onBookClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .semantics {
                    contentDescription = "Book tickets"
                    role = Role.Button
                },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MovieOrange,
                contentColor = MovieWhite
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = elevation,
                pressedElevation = 1.dp
            ),
            interactionSource = interactionSource
        ) {
            Text(
                text = "Book Tickets",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }
    }
}
