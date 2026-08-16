package com.entrymyslot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Theme Colors (Matching your Login Screen) ---
val SurfaceGrey = Color(0xFF1E2126)
val PrimaryOrange = Color(0xFFFF8A00)

@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = { HomeBottomNavigationBar() },
        containerColor = SolidDarkGrey
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Top Location Bar
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TopLocationBar()
            }

            // 2. Search Bar
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SearchBar()
            }

            // 3. Sponsored Banner
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SponsoredBanner()
            }

            // 4. Top Rated Near You
            item {
                Spacer(modifier = Modifier.height(28.dp))
                TopRatedSection()
            }

            // 5. Advertisement Banner
            item {
                Spacer(modifier = Modifier.height(24.dp))
                AdBanner()
            }

            // 6. Explore Sports Grid
            item {
                Spacer(modifier = Modifier.height(28.dp))
                ExploreSportsSection()
            }
        }
    }
}

@Composable
fun TopLocationBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = TextMuted, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Current Location", fontSize = 10.sp, color = TextMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Erode, Tamil Nadu, India", fontSize = 14.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Drop", tint = TextWhite)
                }
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceGrey)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.NotificationsNone, contentDescription = "Alerts", tint = TextWhite)
            // Notification Badge
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PrimaryOrange)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            )
        }
    }
}

@Composable
fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(SurfaceGrey)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = "Search turfs, sports, venues", color = TextMuted, fontSize = 14.sp)
    }
}

@Composable
fun SponsoredBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1B2838), Color(0xFF0F1722)) // Moody game gradient
                )
            )
    ) {
        // Here you will replace with AsyncImage for real banner
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SPONSORED",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "BOOK YOUR NEXT\nTURF AT 20% OFF.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "LIVE MATCH STREAMING", color = PrimaryOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TopRatedSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "TOP RATED NEAR YOU", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "View All", color = PrimaryOrange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { })
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(3) { index ->
                TurfCard(
                    title = if(index == 0) "CHENNAI SPORTS\nCOMPLEX" else "TURF MASTER\nGROUND",
                    sport = "Tennis",
                    price = if(index == 0) "290" else "250"
                )
            }
        }
    }
}

@Composable
fun TurfCard(title: String, sport: String, price: String) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(BorderGrey)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = PrimaryOrange, modifier = Modifier.size(12.dp))
                        Text(text = "4.8", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Sport: $sport", color = TextMuted, fontSize = 11.sp)
                Text(text = "Starting: ₹$price", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = "Offer if available", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AdBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceGrey)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Airplane icon placeholder
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E3A8A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Flight, contentDescription = "Flight", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "ADVERTISEMENT - INDIGO AIRLINES: FLY YOUR DREAMS. BOOK NOW",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp).clickable { })
    }
}

@Composable
fun ExploreSportsSection() {
    val sportsList = listOf(
        "Cricket", "Badminton", "Football", "Tennis",
        "Kabaddi", "Swimming", "Indoor Games", "PS5 / PC Gaming"
    )

    Column {
        Text(text = "EXPLORE SPORTS", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Emulating a 4-column Grid exactly as in the image
        val chunkedList = sportsList.chunked(4)
        chunkedList.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { sport ->
                    SportItem(name = sport)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SportItem(name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        // Image Placeholder mimicking the cool shapes in the image
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceGrey)
                .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            color = TextWhite,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HomeBottomNavigationBar() {
    val items = listOf("Sports / Turf", "Events", "Concerts", "Movies", "Profile")
    val icons = listOf(Icons.Default.SportsBasketball, Icons.Default.Event, Icons.Default.Mic, Icons.Default.Movie, Icons.Default.Person)

    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar(
        containerColor = SolidDarkGrey,
        contentColor = TextWhite,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item,
                        tint = if (selectedItem == index) PrimaryOrange else TextMuted
                    )
                },
                label = {
                    Text(
                        text = item,
                        fontSize = 9.sp,
                        color = if (selectedItem == index) PrimaryOrange else TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = selectedItem == index,
                onClick = { selectedItem = index },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SurfaceGrey
                )
            )
        }
    }
}