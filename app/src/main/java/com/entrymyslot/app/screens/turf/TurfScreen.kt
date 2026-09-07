package com.entrymyslot.app.screens.turf


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.Shower
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
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
import com.entrymyslot.app.data.model.Turf

private val TurfBackground = Color(0xFF061A38)
private val TurfSurface = Color(0xFF0B274F)
private val TurfSurfaceRaised = Color(0xFF0D2D5A)
private val TurfBorder = Color(0xFF24527D)
private val TurfAccent = Color(0xFFFA580B)
private val TurfPrimaryText = Color(0xFFF8FAFF)
private val TurfSecondaryText = Color(0xFFA8B8CF)
private val TurfMutedText = Color(0xFF7185A1)

@Composable
fun TurfScreen(
    onBackClick: () -> Unit = {},
    onBookNowClick: () -> Unit = {},
    sportId: String
) {
    val turfViewModel: TurfViewModel = viewModel(key = "turf_details_$sportId")
    val state by turfViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sportId) {
        turfViewModel.loadTurf(sportId)
    }

    val turf = state.turf
    when {
        turf != null -> TurfDetailsContent(
            turf = turf,
            onBackClick = onBackClick,
            onBookNowClick = onBookNowClick
        )
        state.isLoading -> TurfDetailLoadingState(onBackClick)
        else -> TurfDetailErrorState(
            message = state.errorMessage ?: "Turf details are unavailable.",
            onBackClick = onBackClick,
            onRetry = turfViewModel::retry
        )
    }
}

@Composable
private fun TurfDetailsContent(
    turf: Turf,
    onBackClick: () -> Unit,
    onBookNowClick: () -> Unit
) {
    val sportId = turf.id
    val title = turf.title
    val venueType = turf.venueType
    val price = turf.price.ifBlank { "—" }
    val about = turf.description
    val venueSpecifications = turf.specifications
    val venueRules = turf.rules

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {
            item(key = "venue_hero") {
                TurfHero(
                    turf = turf,
                    onBackClick = onBackClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "directions") {
                val context = LocalContext.current
                Box(Modifier.padding(horizontal = 18.dp)) {
                    DirectionsAction { context.openVenueLocation(turf.location) }
                }
            }

            item(key = "about") {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeading(title = "About this Venue")
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = about,
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color = TurfSecondaryText,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item(key = "facilities") {
                SectionHeading(title = "Facilities")
                Spacer(modifier = Modifier.height(10.dp))
                FacilitiesGrid(turf = turf)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item(key = "venue_specifications") {
                VenueSpecifications(specifications = venueSpecifications)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item(key = "rules") {
                RulesAndGuidelines(rules = venueRules)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        TurfBookingBar(
            price = price,
            onBookNowClick = onBookNowClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TurfHero(
    turf: Turf,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        ) {
            AsyncImage(
                model = turf.imageUrls.firstOrNull() ?: turf.imageUrl ?: R.drawable.turf_hero,
                contentDescription = "${turf.title} cover",
                placeholder = painterResource(R.drawable.turf_hero),
                error = painterResource(R.drawable.turf_hero),
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
                                0.72f to TurfBackground.copy(alpha = 0.62f),
                                1f to TurfBackground
                            )
                        )
                    )
            )

            PremiumTurfBackButton(
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
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = turf.title,
                    color = TurfPrimaryText,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★ ${turf.rating?.toString() ?: "—"}",
                        color = TurfAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(3.dp).clip(CircleShape).background(TurfSecondaryText.copy(alpha = 0.6f)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = turf.venueType,
                        color = TurfPrimaryText.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (turf.location.isNotBlank()) {
                    Spacer(Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                        Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = TurfAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = turf.location,
                            color = TurfPrimaryText.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumTurfBackButton(
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
            tint = TurfPrimaryText,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun TurfDetailLoadingState(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        PremiumTurfBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        )
        PremiumLoadingState(
            modifier = Modifier.align(Alignment.Center).fillMaxSize(),
            message = "Loading venue details..."
        )
    }
}



@Composable
private fun TurfDetailErrorState(
    message: String,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        PremiumTurfBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        )
        PremiumErrorState(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
            title = "Venue Load Failed",
            message = message,
            onRetry = onRetry
        )
    }
}

@Composable
private fun SectionHeading(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 18.dp),
        color = TurfPrimaryText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun FacilitiesGrid(turf: Turf) {
    val facilities = turf.facilities.map { label ->
        val icon = when (label) {
            "Swimming" -> Icons.Outlined.WaterDrop
            "Floodlights" -> Icons.Outlined.LightMode
            "Changing Room" -> Icons.Outlined.Shower
            "Parking" -> Icons.Outlined.LocalParking
            else -> Icons.Outlined.SportsSoccer
        }
        icon to label
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        facilities.chunked(4).forEach { rowFacilities ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowFacilities.forEach { facility ->
                    FacilityItem(icon = facility.first, title = facility.second, modifier = Modifier.weight(1f))
                }
                repeat(4 - rowFacilities.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun FacilityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .semantics { contentDescription = title },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(TurfAccent.copy(alpha = 0.11f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TurfAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            textAlign = TextAlign.Center,
            color = TurfSecondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            lineHeight = 13.sp,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun VenueSpecifications(specifications: List<Pair<String, String>>) {
    SectionHeading(title = "Venue Specifications")
    Spacer(modifier = Modifier.height(8.dp))
    if (specifications.isEmpty()) {
        Text(
            text = "Venue dimensions will be shown when provided.",
            modifier = Modifier.padding(horizontal = 18.dp),
            color = TurfMutedText,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(specifications, key = { it.first }) { specification ->
                Column {
                    Text(
                        text = specification.first.uppercase(),
                        color = TurfMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = specification.second,
                        color = TurfPrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RulesAndGuidelines(rules: List<String>) {
    SectionHeading(title = "Venue Rules")
    Spacer(modifier = Modifier.height(10.dp))
    Column(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val allRules = rules.ifEmpty { listOf("Venue-specific rules will be available before booking.") }
        allRules.forEach { rule ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(TurfAccent))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = rule,
                    color = TurfSecondaryText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SportChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) TurfAccent else TurfSurfaceRaised)
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) TurfAccent else TurfBorder.copy(alpha = 0.72f)
                ),
                RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 15.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (selected) Color.White else TurfSecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TurfBookingBar(
    price: String,
    onBookNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "bookNowScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(TurfBackground.copy(alpha = 0.98f))
            .border(
                BorderStroke(1.dp, TurfBorder.copy(alpha = 0.58f)),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Starting from",
                color = TurfSecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = price,
                color = TurfPrimaryText,
                fontSize = 19.sp,
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
                .clip(RoundedCornerShape(12.dp))
                .background(TurfAccent)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = "Book Now",
                    onClick = onBookNowClick
                )
                .padding(horizontal = 25.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Book Now",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun Context.openVenueLocation(location: String) {
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
        runCatching { startActivity(browserFallbackIntent) }
    }
}


@Composable
private fun DirectionsAction(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val color by animateColorAsState(
        targetValue = if (isPressed) TurfAccent.copy(alpha = 0.22f) else TurfAccent.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = 110),
        label = "directionsColor"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .border(
                BorderStroke(1.dp, TurfAccent.copy(alpha = 0.34f)),
                RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Get Directions",
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Directions",
            color = TurfAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = TurfAccent,
            modifier = Modifier.size(14.dp)
        )
    }
}
