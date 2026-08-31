package com.entrymyslot.app.screens.events

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.entrymyslot.app.R
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.screens.home.PopularEvent

private val EventsBackground = Color(0xFF061A38)
private val EventsSurface = Color(0xFF0B274F)
private val EventsSurfacePressed = Color(0xFF0D2D5A)
private val EventsBorder = Color(0xFF24527D)
private val EventsAccent = Color(0xFFFA580B)
private val EventsPrimaryText = Color(0xFFF8FAFF)
private val EventsSecondaryText = Color(0xFFA8B8CF)
private val EventsMutedText = Color(0xFF7185A1)

@Composable
fun EventsListScreen(
    title: String = "Events",
    events: List<PopularEvent>,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onEventClick: (PopularEvent) -> Unit
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
            EventsHeader(
                title = title,
                onBackClick = onBackClick,
                onSearchClick = onSearchClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = events,
                    key = { event -> event.id }
                ) { event ->
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
private fun EventsHeader(
    title: String,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PremiumBackButton(onClick = onBackClick)

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = EventsPrimaryText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Discover what’s happening",
                color = EventsSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search events",
            tint = EventsAccent,
            modifier = Modifier.size(40.dp).padding(9.dp).clickable(onClick = onSearchClick)
        )
    }
}

@Composable
private fun PremiumBackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "eventsBackScale"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Go back",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = EventsPrimaryText,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun EventListItem(
    event: PopularEvent,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "eventCardScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        animationSpec = tween(durationMillis = 120),
        label = "eventCardElevation"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) {
            EventsAccent.copy(alpha = 0.68f)
        } else {
            EventsBorder.copy(alpha = 0.82f)
        },
        animationSpec = tween(durationMillis = 120),
        label = "eventCardBorder"
    )
    val surfaceColor by animateColorAsState(
        targetValue = if (isPressed) EventsSurfacePressed else EventsSurface,
        animationSpec = tween(durationMillis = 120),
        label = "eventCardSurface"
    )
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .shadow(
                elevation = elevation,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = Color.Black.copy(alpha = 0.32f)
            )
            .clip(cardShape)
            .background(surfaceColor)
            .border(BorderStroke(1.dp, borderColor), cardShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Open ${event.title}",
                onClick = onClick
            )
    ) {
        EventImage(
            event = event,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 12.dp)
        ) {
            Text(
                text = event.title,
                color = EventsPrimaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            EventMetadataRow(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = EventsAccent,
                        modifier = Modifier.size(15.dp)
                    )
                },
                text = event.date
            )

            Spacer(modifier = Modifier.height(6.dp))

            EventMetadataRow(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = EventsMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                },
                text = event.location
            )
        }
    }
}

@Composable
private fun EventImage(
    event: PopularEvent,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFF123E70), Color(0xFF081F42))
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = event.imageUrl ?: R.drawable.event_fallback,
            contentDescription = event.title,
            placeholder = painterResource(R.drawable.event_fallback),
            error = painterResource(R.drawable.event_fallback),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            EventsBackground.copy(alpha = 0.68f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(EventsBackground.copy(alpha = 0.90f))
                .border(
                    width = 1.dp,
                    color = EventsAccent.copy(alpha = 0.42f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 11.dp, vertical = 7.dp)
        ) {
            Text(
                text = event.price,
                color = EventsAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EventMetadataRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = EventsSecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
