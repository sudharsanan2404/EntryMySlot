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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.entrymyslot.app.screens.home.GlowBackground

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
    sportId: String = "sport_1"
) {
    val title = when (sportId) {
        "sport_1" -> "Green Arena Turf"
        "sport_2" -> "Blue Wave Pool"
        "sport_3" -> "Elite Badminton Club"
        else -> "Sports Venue"
    }
    val venueType = when (sportId) {
        "sport_1" -> "5-a-side Turf"
        "sport_2" -> "Aquatic Centre"
        "sport_3" -> "Indoor Court"
        else -> "Sports Venue"
    }
    val price = when (sportId) {
        "sport_1" -> "₹800 / hour"
        "sport_2" -> "₹200 / hour"
        "sport_3" -> "₹400 / hour"
        else -> "Contact for Price"
    }
    val about = when (sportId) {
        "sport_1" -> "Premium 5-a-side football turf with professional artificial grass, floodlights and comfortable facilities. Perfect for casual games and tournaments."
        "sport_2" -> "Pristine Olympic-sized swimming pool with temperature control and dedicated lanes for professional training and recreational swimming."
        "sport_3" -> "State-of-the-art indoor badminton facility featuring 4 professional wooden courts with synthetic mats and excellent LED lighting."
        else -> "Premium sports facility with modern amenities and professional standards."
    }
    val sports = when (sportId) {
        "sport_1" -> listOf("Football", "Cricket")
        "sport_2" -> listOf("Swimming", "Diving")
        "sport_3" -> listOf("Badminton", "Table Tennis")
        else -> listOf("Sports")
    }
    val venueImages = emptyList<String>()
    val venueSpecifications = emptyList<Pair<String, String>>()
    val venueRules = emptyList<String>()
    val venueLocation = "Chennai, Tamil Nadu"
    val context = LocalContext.current

    var isFavorite by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {
            item(key = "header") {
                TurfHeader(
                    isFavorite = isFavorite,
                    onBackClick = onBackClick,
                    onFavoriteClick = { isFavorite = !isFavorite }
                )
            }

            item(key = "venue_hero") {
                VenueImageCarousel(
                    sportId = sportId,
                    venueTitle = title,
                    imageUrls = venueImages
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "venue_identity") {
                VenueIdentity(
                    title = title,
                    venueType = venueType,
                    location = venueLocation,
                    onDirectionsClick = { context.openVenueLocation(venueLocation) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item(key = "about") {
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
                FacilitiesGrid(sportId = sportId)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item(key = "venue_specifications") {
                VenueSpecifications(specifications = venueSpecifications)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item(key = "available_sports") {
                SectionHeading(title = "Available Sports")
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sports, key = { _, sport -> sport }) { index, sport ->
                        SportChip(
                            name = sport,
                            selected = index == 0
                        )
                    }
                }
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
private fun TurfHeader(
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(68.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderBackButton(onClick = onBackClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Venue Details",
            modifier = Modifier.weight(1f),
            color = TurfPrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        FavoriteButton(
            isFavorite = isFavorite,
            onClick = onFavoriteClick
        )
    }
}

@Composable
private fun HeaderBackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "turfBackScale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TurfSurface.copy(alpha = 0.94f))
                .border(BorderStroke(1.dp, TurfBorder), CircleShape),
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
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "favoriteScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isFavorite) {
            TurfAccent.copy(alpha = 0.18f)
        } else {
            TurfSurface.copy(alpha = 0.94f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "favoriteContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFavorite) TurfAccent else TurfBorder,
        animationSpec = tween(durationMillis = 150),
        label = "favoriteBorder"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = if (isFavorite) "Remove favorite" else "Add favorite",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(containerColor)
                .border(BorderStroke(1.dp, borderColor), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) {
                    Icons.Rounded.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                contentDescription = "Favorite",
                tint = if (isFavorite) TurfAccent else TurfPrimaryText,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun VenueImageCarousel(
    sportId: String,
    venueTitle: String,
    imageUrls: List<String>
) {
    val icon = when (sportId) {
        "sport_2" -> Icons.Outlined.WaterDrop
        else -> Icons.Outlined.SportsSoccer
    }
    val pages: List<String?> = if (imageUrls.isEmpty()) listOf(null) else imageUrls
    val listState = rememberLazyListState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(184.dp)
    ) {
        val pageWidth = maxWidth
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(pages) { index, imageUrl ->
                VenueImagePage(
                    imageUrl = imageUrl,
                    venueTitle = venueTitle,
                    icon = icon,
                    modifier = Modifier.width(pageWidth),
                    page = index + 1
                )
            }
        }

        if (pages.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TurfBackground.copy(alpha = 0.72f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (listState.firstVisibleItemIndex == index) 7.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (listState.firstVisibleItemIndex == index) {
                                    TurfAccent
                                } else {
                                    TurfSecondaryText.copy(alpha = 0.52f)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun VenueImagePage(
    imageUrl: String?,
    venueTitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    page: Int
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(17.dp),
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.24f)
            )
            .clip(RoundedCornerShape(17.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF102B50), Color(0xFF07162C))
                )
            )
            .border(
                BorderStroke(1.dp, TurfBorder.copy(alpha = 0.72f)),
                RoundedCornerShape(17.dp)
            )
            .semantics {
                contentDescription = "$venueTitle venue image $page"
            },
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(TurfAccent.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TurfAccent,
                    modifier = Modifier.size(31.dp)
                )
            }
        }

        Text(
            text = "ENTRYMYSLOT SPORTS",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            color = TurfSecondaryText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        )
    }
}

@Composable
private fun VenueIdentity(
    title: String,
    venueType: String,
    location: String,
    onDirectionsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        Text(
            text = title,
            color = TurfPrimaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 29.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "★ 4.7",
                color = TurfAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "  •  $venueType",
                modifier = Modifier.weight(1f),
                color = TurfSecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = "Location",
                tint = TurfAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Column {
                Text(
                    text = "2.4 km away",
                    color = TurfPrimaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = location,
                    color = TurfSecondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            DirectionsAction(onClick = onDirectionsClick)
        }
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
private fun FacilitiesGrid(sportId: String) {
    val mainIcon = when (sportId) {
        "sport_2" -> Icons.Outlined.WaterDrop
        else -> Icons.Outlined.SportsSoccer
    }
    val mainLabel = when (sportId) {
        "sport_1" -> "Football"
        "sport_2" -> "Swimming"
        "sport_3" -> "Badminton"
        else -> "Sports"
    }

    val facilities = listOf(
        mainIcon to mainLabel,
        Icons.Outlined.LightMode to "Floodlights",
        Icons.Outlined.Shower to "Changing Room",
        Icons.Outlined.LocalParking to "Parking"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items(facilities, key = { it.second }) { facility ->
            FacilityItem(icon = facility.first, title = facility.second)
        }
    }
}

@Composable
private fun FacilityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Column(
        modifier = Modifier
            .width(74.dp)
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
            color = TurfSecondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            lineHeight = 13.sp,
            overflow = TextOverflow.Ellipsis
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
    SectionHeading(title = "Rules & Guidelines")
    Spacer(modifier = Modifier.height(8.dp))
    Column(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (rules.isEmpty()) {
            Text(
                text = "Venue-specific rules will be available before booking.",
                color = TurfMutedText,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        } else {
            rules.forEach { rule ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "•",
                        color = TurfAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rule,
                        color = TurfSecondaryText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SportChip(
    name: String,
    selected: Boolean
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
