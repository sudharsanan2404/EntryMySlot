package com.entrymyslot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.ui.theme.*

@Composable
fun TurfDetailScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        containerColor = SolidDarkGrey,
        bottomBar = { DetailsBottomBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Image Carousel with Top Bar Overlay
            item { TurfImageCarousel(onBackClick) }

            // 2. Header Info (Name, Location, Distance)
            item { TurfHeaderInfo() }

            // 3. Highlight Badges (Indoor, 5v5, etc.)
            item { TurfBadges() }

            // 4. Dimensions & Surface
            item { TurfSpecifications() }

            // 5. Amenities / Features
            item { TurfAmenities() }

            // 6. Reviews Section
            item { TurfReviewsSummary() }
        }
    }
}

@Composable
fun TurfImageCarousel(onBackClick: () -> Unit) {
    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Image Pager (Using placeholders for now)
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF2C3E50), Color(0xFF000000))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.SportsSoccer, contentDescription = "Turf", tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(100.dp))
            }
        }

        // Gradient overlay at bottom for indicator visibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, SolidDarkGrey)))
        )

        // Dot Indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) PrimaryOrange else Color.White.copy(alpha = 0.5f)
                val width = if (pagerState.currentPage == iteration) 16.dp else 6.dp
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = width, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }

        // Top App Bar Icons (Floating)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Row {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Save", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun TurfHeaderInfo() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Erode Titans Turf", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = PrimaryOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Perundurai Road, Erode", fontSize = 13.sp, color = TextMuted)
                }
            }
            // Rating Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceGrey)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "4.8", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                }
                Text(text = "120+ Reviews", fontSize = 9.sp, color = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Distance Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryOrange.copy(alpha = 0.1f))
                .border(1.dp, PrimaryOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.DirectionsCar, contentDescription = "Drive", tint = PrimaryOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "5.2 km away from your location", fontSize = 13.sp, color = PrimaryOrange, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TurfBadges() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BadgeChip("Outdoor", Icons.Outlined.WbSunny)
        BadgeChip("5v5 / 7v7", Icons.Outlined.Groups)
        BadgeChip("Artificial Grass", Icons.Outlined.Grass)
    }
}

@Composable
fun BadgeChip(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGrey)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = text, tint = TextMuted, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TurfSpecifications() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Pitch Specifications", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecCard(title = "Dimensions", value = "110 ft x 60 ft", modifier = Modifier.weight(1f))
            SpecCard(title = "Height Clearance", value = "30 ft Netting", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SpecCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceGrey)
            .padding(12.dp)
    ) {
        Text(text = title, fontSize = 11.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
    }
}

@Composable
fun TurfAmenities() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Amenities Available", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(16.dp))

        // Hardcoded Grid for UI visualization
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AmenityItem("Parking", Icons.Outlined.LocalParking, Modifier.weight(1f))
                AmenityItem("Washroom", Icons.Outlined.Wc, Modifier.weight(1f))
                AmenityItem("Floodlights", Icons.Outlined.Lightbulb, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AmenityItem("Drinking Water", Icons.Outlined.WaterDrop, Modifier.weight(1f))
                AmenityItem("Bibs Provided", Icons.Outlined.Checkroom, Modifier.weight(1f))
                AmenityItem("First Aid", Icons.Outlined.MedicalServices, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AmenityItem(name: String, icon: ImageVector, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceGrey),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = name, tint = PrimaryOrange, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TurfReviewsSummary() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "User Reviews", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))

        // Single dummy review
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGrey)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("S", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Sathish Kumar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(text = "2 days ago", fontSize = 10.sp, color = TextMuted)
                }
                Row {
                    repeat(5) { Icon(Icons.Default.Star, contentDescription = "Star", tint = PrimaryOrange, modifier = Modifier.size(12.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Great turf in Erode! The artificial grass is well maintained and floodlights are super bright for night matches. Washrooms are clean.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun DetailsBottomBar() {
    Surface(
        color = SurfaceGrey.copy(alpha = 0.95f),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Price per hour", fontSize = 11.sp, color = TextMuted)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "₹800", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                    Text(text = " onwards", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(bottom = 3.dp))
                }
            }

            Button(
                onClick = { /* TODO: Navigate to Booking Page */ },
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(PrimaryOrange, PrimaryOrangeEnd)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Book Now", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}