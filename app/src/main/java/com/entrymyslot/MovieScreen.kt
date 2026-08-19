package com.entrymyslot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.ui.theme.*

@Composable
fun MovieScreen() {
    Scaffold(
        containerColor = SolidDarkGrey
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Location & Search Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MovieTopHeader()
            }

            // 2. Big Movie Banner Carousel
            item {
                Spacer(modifier = Modifier.height(24.dp))
                MovieBannerCarousel()
            }

            // 3. Language & Genre Filters
            item {
                Spacer(modifier = Modifier.height(24.dp))
                MovieFilters()
            }

            // 4. Now Showing (Erode) Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                NowShowingSection()
            }

            // 5. Coming Soon Section
            item {
                Spacer(modifier = Modifier.height(32.dp))
                ComingSoonSection()
            }
        }
    }
}

@Composable
fun MovieTopHeader() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Watching movies in", fontSize = 11.sp, color = TextMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Erode", fontSize = 16.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Drop", tint = PrimaryOrange)
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
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextWhite)
            }
        }
    }
}

@Composable
fun MovieBannerCarousel() {
    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (page % 2 == 0) listOf(Color(0xFF4B1010), Color(0xFF1A0505))
                            else listOf(Color(0xFF0F2027), Color(0xFF203A43))
                        )
                    )
            ) {
                // Banner Content Placeholder
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "PREMIERES TODAY",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(RatingRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (page == 0) "THE EPIC CLASH" else "ACTION BLOCKBUSTER",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(text = "Book your tickets now", color = TextMuted, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Dots Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) PrimaryOrange else BorderGrey
                val width = if (pagerState.currentPage == iteration) 16.dp else 6.dp
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = width, height = 6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun MovieFilters() {
    val filters = listOf("Tamil", "English", "3D", "IMAX", "Action", "Comedy")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters.size) { index ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, BorderGrey, RoundedCornerShape(20.dp))
                    .background(if (index == 0) PrimaryOrange.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filters[index],
                    color = if (index == 0) PrimaryOrange else TextWhite,
                    fontSize = 12.sp,
                    fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun NowShowingSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Now Showing", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "See All", color = PrimaryOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of Movies (Using rows for 2-column layout)
        val movies = listOf(
            Triple("Thalapathy 69", "Tamil • 2D", "8.9"),
            Triple("Deadpool & Wolverine", "English • 3D", "9.2"),
            Triple("Kanguva", "Tamil • 3D", "8.5"),
            Triple("Amaran", "Tamil • 2D", "9.0")
        )

        movies.chunked(2).forEach { rowMovies ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowMovies.forEach { movie ->
                    MovieCard(
                        title = movie.first,
                        desc = movie.second,
                        rating = movie.third,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Handle odd number of items
                if (rowMovies.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ComingSoonSection() {
    Column {
        Text(
            text = "Coming Soon",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(3) { index ->
                MovieCard(
                    title = if (index == 0) "Pushpa 2" else "Viduthalai Part 2",
                    desc = "Action • Drama",
                    rating = null, // No rating for coming soon
                    isComingSoon = true,
                    modifier = Modifier.width(140.dp)
                )
            }
        }
    }
}

@Composable
fun MovieCard(
    title: String,
    desc: String,
    rating: String?,
    isComingSoon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.clickable { /* Navigate to Theatre Selection */ }) {
        // Poster Box (Standard 2:3 aspect ratio for movies)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.66f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGrey)
                .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
        ) {
            // Heart Icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            // Rating/Date Badge at the bottom of the poster
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isComingSoon) {
                    Text(text = "15 Aug, 2026", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = RatingRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$rating/10  •  25K Votes", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Movie Details
        Text(
            text = title,
            color = TextWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            color = TextMuted,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}