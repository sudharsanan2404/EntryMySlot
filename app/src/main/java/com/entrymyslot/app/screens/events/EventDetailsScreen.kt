package com.entrymyslot.app.screens.events

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.entrymyslot.app.screens.home.PopularEvent

private val DetailsBackground = Color(0xFF061A38)
private val DetailsSurface = Color(0xFF0B274F)
private val DetailsSurfaceRaised = Color(0xFF0D2D5A)
private val DetailsBorder = Color(0xFF24527D)
private val DetailsAccent = Color(0xFFFA580B)
private val DetailsPrimaryText = Color(0xFFF8FAFF)
private val DetailsSecondaryText = Color(0xFFA8B8CF)
private val DetailsMutedText = Color(0xFF7185A1)

@Composable
fun EventDetailsScreen(
    event: PopularEvent,
    onBackClick: () -> Unit,
    onBookTicketsClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailsBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 118.dp)
        ) {
            item(key = "event_hero") {
                EventHero(
                    event = event,
                    onBackClick = onBackClick
                )
            }

            item(key = "event_information") {
                EventInformationSection(
                    event = event,
                    onGoToLocationClick = {
                        context.openEventLocation(event.location)
                    }
                )
            }

            item(key = "about_event") {
                AboutEventSection(
                    description = "Review the schedule and venue details for ${event.title}. " +
                        "When you’re ready, continue to choose your ticket category and quantity."
                )
            }

            item(key = "ticket_preview") {
                TicketPreview(price = event.price)
            }
        }

        EventDetailsBottomBar(
            price = event.price,
            onBookTicketsClick = onBookTicketsClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun EventHero(
    event: PopularEvent,
    onBackClick: () -> Unit
) {
    var contentVisible by remember(event.id) { mutableStateOf(false) }
    LaunchedEffect(event.id) { contentVisible = true }
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "eventHeroAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(352.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF123E70), Color(0xFF081F42))
                )
            )
    ) {
        if (event.imageUrl != null) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            EventHeroFallback(eventTitle = event.title)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to DetailsBackground.copy(alpha = 0.58f),
                        0.27f to Color.Transparent,
                        0.56f to Color.Transparent,
                        1f to DetailsBackground.copy(alpha = 0.98f)
                    )
                )
        )

        PremiumHeroBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 10.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = (1f - contentAlpha) * 14.dp.toPx()
                }
                .padding(horizontal = 18.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DetailsAccent.copy(alpha = 0.16f))
                    .border(
                        BorderStroke(1.dp, DetailsAccent.copy(alpha = 0.42f)),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = null,
                    tint = DetailsAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "EVENT",
                    color = DetailsPrimaryText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(11.dp))

            Text(
                text = event.title,
                color = DetailsPrimaryText,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 32.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            HeroMetadata(
                icon = Icons.Rounded.CalendarToday,
                text = event.date
            )
            Spacer(modifier = Modifier.height(6.dp))
            HeroMetadata(
                icon = Icons.Rounded.LocationOn,
                text = event.location
            )
        }
    }
}

@Composable
private fun EventHeroFallback(eventTitle: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(DetailsAccent.copy(alpha = 0.13f))
                .border(
                    width = 1.dp,
                    color = DetailsAccent.copy(alpha = 0.36f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Event,
                contentDescription = eventTitle,
                tint = DetailsAccent,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "ENTRYMYSLOT EVENTS",
            color = DetailsSecondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun HeroMetadata(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DetailsAccent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = DetailsSecondaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PremiumHeroBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "eventDetailsBackScale"
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(5.dp, CircleShape)
            .clip(CircleShape)
            .background(DetailsBackground.copy(alpha = 0.86f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)), CircleShape)
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
            tint = DetailsPrimaryText,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun EventInformationSection(
    event: PopularEvent,
    onGoToLocationClick: () -> Unit
) {
    DetailsSection(
        title = "Event Information",
        modifier = Modifier.padding(top = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DetailsSurface)
                .border(
                    BorderStroke(1.dp, DetailsBorder.copy(alpha = 0.82f)),
                    RoundedCornerShape(18.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InformationRow(
                icon = Icons.Rounded.CalendarToday,
                label = "DATE & TIME",
                value = event.date
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DetailsBorder.copy(alpha = 0.45f))
            )
            InformationRow(
                icon = Icons.Rounded.LocationOn,
                label = "VENUE",
                value = event.location
            )
            LocationAction(onClick = onGoToLocationClick)
        }
    }
}

@Composable
private fun InformationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(DetailsAccent.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DetailsAccent,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = DetailsMutedText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                color = DetailsPrimaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun LocationAction(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "locationActionScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(DetailsSurfaceRaised)
            .border(
                BorderStroke(1.dp, DetailsAccent.copy(alpha = 0.30f)),
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Go to Location",
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = DetailsAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = "Go to Location",
            modifier = Modifier.weight(1f),
            color = DetailsPrimaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = DetailsSecondaryText,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AboutEventSection(description: String) {
    DetailsSection(title = "About the Event") {
        Text(
            text = description,
            color = DetailsSecondaryText,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun TicketPreview(price: String) {
    DetailsSection(title = "Ticket Information") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DetailsSurface)
                .border(
                    BorderStroke(1.dp, DetailsBorder.copy(alpha = 0.82f)),
                    RoundedCornerShape(18.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(DetailsAccent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = DetailsAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AVAILABLE TICKETS",
                    color = DetailsMutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.9.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = price,
                    color = DetailsAccent,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DetailsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = DetailsPrimaryText,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun EventDetailsBottomBar(
    price: String,
    onBookTicketsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "bookTicketsScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(DetailsBackground.copy(alpha = 0.98f))
            .border(
                width = 1.dp,
                color = DetailsBorder.copy(alpha = 0.58f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tickets",
                color = DetailsSecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = price,
                color = DetailsPrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .clip(RoundedCornerShape(13.dp))
                .background(DetailsAccent)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = "Book Tickets",
                    onClick = onBookTicketsClick
                )
                .padding(horizontal = 25.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Book Tickets",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun Context.openEventLocation(location: String) {
    val encodedLocation = Uri.encode(location)
    val googleMapsIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=$encodedLocation")
    ).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val browserFallbackIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedLocation")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val openedGoogleMaps = runCatching {
        startActivity(googleMapsIntent)
        true
    }.getOrDefault(false)

    if (!openedGoogleMaps) {
        runCatching {
            startActivity(browserFallbackIntent)
        }
    }
}
