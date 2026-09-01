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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.data.model.Event

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
    eventId: String,
    onBackClick: () -> Unit,
    onBookTicketsClick: () -> Unit
) {
    val app = LocalContext.current.applicationContext as EntryMySlotApp
    val eventViewModel: EventViewModel = viewModel(
        key = "event_details_$eventId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EventViewModel(
                    detailsApi = app.appContainer.detailsApi,
                    networkMonitor = app.appContainer.networkMonitor
                ) as T
        }
    )
    val state by eventViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) {
        eventViewModel.loadEvent(eventId)
    }

    val event = state.event
    when {
        event != null -> EventDetailsContent(
            event = event,
            onBackClick = onBackClick,
            onBookTicketsClick = onBookTicketsClick
        )
        state.isLoading -> EventDetailLoadingState(onBackClick)
        else -> EventDetailErrorState(
            message = state.errorMessage ?: "Event details are unavailable.",
            onBackClick = onBackClick,
            onRetry = eventViewModel::retry
        )
    }
}

@Composable
private fun EventDetailsContent(
    event: Event,
    onBackClick: () -> Unit,
    onBookTicketsClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

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


            item(key = "interest") {
                EventInterestCard(event)
            }

            item(key = "about_event") {
                AboutEventSection(
                    description = event.description
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
    event: Event,
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
            .height(320.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF123E70), Color(0xFF081F42))
                )
            )
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
                    translationY = (1f - contentAlpha) * 12.dp.toPx()
                }
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = event.title,
                color = DetailsPrimaryText,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 29.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarToday, null, tint = DetailsAccent, modifier = Modifier.size(15.dp))
                    Text(
                        event.date,
                        color = DetailsPrimaryText.copy(alpha = 0.88f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = DetailsAccent, modifier = Modifier.size(15.dp))
                    Text(
                        event.location,
                        color = DetailsPrimaryText.copy(alpha = 0.88f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

    }
}

@Composable
private fun EventInterestCard(event: Event) {
    var interested by rememberSaveable(event.id) { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp)).background(DetailsSurface)
            .border(1.dp, DetailsBorder, RoundedCornerShape(14.dp)).padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Interested in this event?", color = DetailsPrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                "Get updates when details or booking information changes.",
                color = DetailsSecondaryText,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 2.dp, end = 6.dp)
            )
        }
        Button(
            onClick = { interested = !interested },
            modifier = Modifier.height(34.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (interested) DetailsSurfaceRaised else DetailsAccent)
        ) { Text(if (interested) "Interested ✓" else "Interested", color = DetailsPrimaryText, fontSize = 10.sp) }
    }
}

@Composable
private fun EventDetailLoadingState(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        PremiumHeroBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DetailsAccent)
            Spacer(modifier = Modifier.height(14.dp))
            Text("Loading event details…", color = DetailsSecondaryText)
        }
    }
}

@Composable
private fun EventDetailErrorState(
    message: String,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        PremiumHeroBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.Event, contentDescription = null, tint = DetailsAccent, modifier = Modifier.size(42.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text(message, color = DetailsPrimaryText, fontSize = 15.sp, lineHeight = 21.sp)
            Spacer(modifier = Modifier.height(18.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DetailsAccent)) {
                Text("Retry", color = DetailsPrimaryText)
            }
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
    event: Event,
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
            .padding(horizontal = 18.dp, vertical = 10.dp)
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
