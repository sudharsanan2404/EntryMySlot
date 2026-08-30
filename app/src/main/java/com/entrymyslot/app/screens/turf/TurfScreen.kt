package com.entrymyslot.app.screens.turf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Shower
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Favorite
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// ------------------------------------------------------------
// COLORS
// ------------------------------------------------------------

private val TurfBlueTop = Color(0xFF063DB5)
private val TurfBlueBottom = Color(0xFF041F5D)

private val TurfCard = Color(0xFF111D32)
private val TurfCardLight = Color(0xFF142B58)

private val TurfOrange = Color(0xFFFF8A00)
private val TurfWhite = Color.White
private val TurfGray = Color(0xFFB8C0D0)


// ------------------------------------------------------------
// TURF SCREEN
// ------------------------------------------------------------

@Composable
fun TurfScreen(
    onBackClick: () -> Unit = {},
    onBookNowClick: () -> Unit = {},
    sportId: String = "sport_1"
) {

    val title = when(sportId) {
        "sport_1" -> "Green Arena Turf"
        "sport_2" -> "Blue Wave Pool"
        "sport_3" -> "Elite Badminton Club"
        else -> "Sports Venue"
    }

    val type = when(sportId) {
        "sport_1" -> "Football • 5v5"
        "sport_2" -> "Swimming • Olympic Size"
        "sport_3" -> "Badminton • 4 Courts"
        else -> "Sports"
    }

    val price = when(sportId) {
        "sport_1" -> "₹800 / hour"
        "sport_2" -> "₹200 / hour"
        "sport_3" -> "₹400 / hour"
        else -> "Contact for Price"
    }

    val about = when(sportId) {
        "sport_1" -> "Premium 5-a-side football turf with professional artificial grass, floodlights and comfortable facilities. Perfect for casual games and tournaments."
        "sport_2" -> "Pristine Olympic-sized swimming pool with temperature control and dedicated lanes for professional training and recreational swimming."
        "sport_3" -> "State-of-the-art indoor badminton facility featuring 4 professional wooden courts with synthetic mats and excellent LED lighting."
        else -> "Premium sports facility with modern amenities and professional standards."
    }

    var isFavorite by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TurfBlueTop,
                        Color(0xFF0737A4),
                        Color(0xFF062E88),
                        TurfBlueBottom
                    )
                )
            )
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                bottom = 110.dp
            )
        ) {

            // ------------------------------------------------
            // TOP BAR
            // ------------------------------------------------

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TurfWhite,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                onBackClick()
                            }
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Color.Black.copy(alpha = 0.15f)
                            )
                            .clickable {
                                isFavorite = !isFavorite
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                if (isFavorite) {
                                    Icons.Rounded.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                            contentDescription = "Favorite",
                            tint =
                                if (isFavorite) {
                                    Color.Red
                                } else {
                                    TurfWhite
                                },
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            // ------------------------------------------------
            // TURF IMAGE
            // ------------------------------------------------

            item {

                TurfImagePlaceholder(sportId = sportId)

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }

            // ------------------------------------------------
            // TURF DETAILS
            // ------------------------------------------------

            item {

                Column(
                    modifier = Modifier.padding(
                        horizontal = 18.dp
                    )
                ) {

                    Text(
                        text = title,
                        color = TurfWhite,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "★ 4.7",
                            color = TurfOrange,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "  •  $type",
                            color = TurfGray,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = TurfOrange,
                            modifier = Modifier.size(21.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(5.dp)
                        )

                        Column {

                            Text(
                                text = "2.4 km away",
                                color = TurfWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Chennai, Tamil Nadu",
                                color = TurfGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(25.dp)
                )
            }

            // ------------------------------------------------
            // ABOUT
            // ------------------------------------------------

            item {

                SectionHeading(
                    title = "About this Venue"
                )

                Text(
                    text = about,
                    color = TurfGray,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 8.dp
                    )
                )

                Spacer(
                    modifier = Modifier.height(22.dp)
                )
            }

            // ------------------------------------------------
            // FACILITIES
            // ------------------------------------------------

            item {

                SectionHeading(
                    title = "Facilities"
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                FacilitiesGrid(sportId = sportId)

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }

            // ------------------------------------------------
            // SPORTS
            // ------------------------------------------------

            item {

                SectionHeading(
                    title = "Available Sports"
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                val sports = when(sportId) {
                    "sport_1" -> listOf("Football", "Cricket")
                    "sport_2" -> listOf("Swimming", "Diving")
                    "sport_3" -> listOf("Badminton", "Table Tennis")
                    else -> listOf("Sports")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    sports.forEachIndexed { index, sport ->
                        SportChip(
                            name = sport,
                            selected = index == 0
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(25.dp)
                )
            }
        }

        // ----------------------------------------------------
        // BOOK NOW BAR
        // ----------------------------------------------------

        TurfBookingBar(
            price = price,
            onBookNowClick = onBookNowClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


// ------------------------------------------------------------
// IMAGE PLACEHOLDER
// ------------------------------------------------------------

@Composable
private fun TurfImagePlaceholder(sportId: String) {

    val icon = when(sportId) {
        "sport_1" -> Icons.Outlined.SportsSoccer
        "sport_2" -> Icons.Outlined.WaterDrop
        "sport_3" -> Icons.Outlined.SportsSoccer // Should be badminton, but let's use something generic or just keep it
        else -> Icons.Outlined.SportsSoccer
    }

    val label = when(sportId) {
        "sport_1" -> "TURF IMAGE"
        "sport_2" -> "POOL IMAGE"
        "sport_3" -> "CLUB IMAGE"
        else -> "VENUE IMAGE"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(215.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Color(0xFF172B4A)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF31528A),
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF667085),
                modifier = Modifier.size(55.dp)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = label,
                color = Color(0xFF78849A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Replace with actual image",
                color = Color(0xFF667085),
                fontSize = 12.sp
            )
        }
    }
}


// ------------------------------------------------------------
// SECTION HEADING
// ------------------------------------------------------------

@Composable
private fun SectionHeading(
    title: String
) {

    Text(
        text = title,
        color = TurfWhite,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(
            horizontal = 18.dp
        )
    )
}


// ------------------------------------------------------------
// FACILITIES
// ------------------------------------------------------------

@Composable
private fun FacilitiesGrid(sportId: String) {

    val mainIcon = when(sportId) {
        "sport_1" -> Icons.Outlined.SportsSoccer
        "sport_2" -> Icons.Outlined.WaterDrop
        "sport_3" -> Icons.Outlined.SportsSoccer
        else -> Icons.Outlined.SportsSoccer
    }

    val mainLabel = when(sportId) {
        "sport_1" -> "Football"
        "sport_2" -> "Swimming"
        "sport_3" -> "Badminton"
        else -> "Sports"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            FacilityItem(
                icon = mainIcon,
                title = mainLabel
            )

            FacilityItem(
                icon = Icons.Outlined.LightMode,
                title = "Floodlights"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            FacilityItem(
                icon = Icons.Outlined.Shower,
                title = "Changing Room"
            )

            FacilityItem(
                icon = Icons.Outlined.LocalParking,
                title = "Parking"
            )
        }
    }
}


// ------------------------------------------------------------
// FACILITY ITEM
// ------------------------------------------------------------

@Composable
private fun RowScope.FacilityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {

    Row(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(TurfCardLight)
            .border(
                width = 1.dp,
                color = Color(0xFF274A86),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 13.dp,
                vertical = 13.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TurfOrange,
            modifier = Modifier.size(23.dp)
        )

        Spacer(
            modifier = Modifier.width(9.dp)
        )

        Text(
            text = title,
            color = TurfWhite,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


// ------------------------------------------------------------
// SPORT CHIP
// ------------------------------------------------------------

@Composable
private fun SportChip(
    name: String,
    selected: Boolean
) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) {
                    TurfOrange
                } else {
                    TurfCardLight
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    TurfOrange
                } else {
                    Color(0xFF31528A)
                },
                shape = RoundedCornerShape(10.dp)
            )
            .padding(
                horizontal = 20.dp,
                vertical = 11.dp
            )
    ) {

        Text(
            text = name,
            color = if (selected) {
                Color.White
            } else {
                TurfGray
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


// ------------------------------------------------------------
// BOOKING BAR
// ------------------------------------------------------------

@Composable
private fun TurfBookingBar(
    price: String,
    onBookNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color(0xFF061F58)
            )
            .navigationBarsPadding()
            .padding(
                horizontal = 18.dp,
                vertical = 13.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "Starting from",
                color = TurfGray,
                fontSize = 12.sp
            )

            Text(
                text = price,
                color = TurfWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(TurfOrange)
                .clickable {
                    onBookNowClick()
                }
                .padding(
                    horizontal = 27.dp,
                    vertical = 13.dp
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Book Now",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}