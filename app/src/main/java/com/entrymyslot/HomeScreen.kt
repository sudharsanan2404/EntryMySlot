package com.entrymyslot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import kotlinx.coroutines.delay
import org.checkerframework.common.subtyping.qual.Bottom

private val EntryOrange = Color(0xFFF05A28)
private val EntryOrangeDark = Color(0xFFE04C1A)
private val EntryDark = Color(0xFF262626)
private val EntryDarkHover = Color(0xFF1A1A1A)

private val LightBackground = Color(0xFFF9FAFB)
private val BorderLight = Color(0xFFE5E7EB)
private val TextDark = Color(0xFF262626)
private val TextGrey = Color(0xFF6B7280)

private data class Turf(
    val name: String,
    val location: String,
    val rating: String,
    val sport: String,
    val type: String,
    val price: String,
    val image: String
)

private data class Sport(
    val name: String,
    val venues: String,
    val icon: @Composable () -> Unit
)

private val turfList = listOf(
    Turf(
        name = "Kickoff Arena",
        location = "Saravanampatti",
        rating = "4.8",
        sport = "Football",
        type = "7v7",
        price = "₹1,200",
        image = "https://images.unsplash.com/photo-1552667466-07770ae110d0?q=80&w=400&auto=format&fit=crop"
    ),
    Turf(
        name = "Smash Court",
        location = "Peelamedu",
        rating = "4.6",
        sport = "Badminton",
        type = "Synthetic",
        price = "₹400",
        image = "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?q=80&w=400&auto=format&fit=crop"
    ),
    Turf(
        name = "Pitch Perfect Box",
        location = "RS Puram",
        rating = "4.9",
        sport = "Box Cricket",
        type = "24x7",
        price = "₹1,500",
        image = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?q=80&w=400&auto=format&fit=crop"
    ),
    Turf(
        name = "The Grand Slam",
        location = "Race Course",
        rating = "4.5",
        sport = "Tennis",
        type = "Clay Court",
        price = "₹600",
        image = "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?q=80&w=400&auto=format&fit=crop"
    )
)

private const val HERO_IMAGE_1 =
    "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?q=80&w=800&auto=format&fit=crop"

private const val HERO_IMAGE_2 =
    "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?q=80&w=800&auto=format&fit=crop"

@Composable
fun HomeScreen() {
    var selectedBottomItem by remember { mutableIntStateOf(0) }

    val bottomItems = remember {
        listOf(
            "Home" to Icons.Default.Home,
            "Search" to Icons.Default.Search,
            "Bookings" to Icons.Default.ConfirmationNumber,
            "Profile" to Icons.Default.Person
        )
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            MobileBottomNavigation(
                selectedItem = selectedBottomItem,
                items = bottomItems,
                onItemSelected = { selectedBottomItem = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            HomeHeader()
            HomeNavigationBar()
            HeroCarousel()
            TopRatedVenues()
            WeekendLeagueBanner()
            BrowseSports()
            HomeFooter()
        }
    }
}

/* -------------------------------------------------------------------------- */
/* HEADER                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun HomeHeader() {
    var showLocation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(EntryDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(R.drawable.logo),
                contentDescription = "EntryMySlot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(100.dp)
                    .height(42.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = {}, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showLocation = !showLocation }
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "CBE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Location",
                        tint = EntryOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showLocation) {
                    Popup(
                        alignment = Alignment.TopCenter,
                        offset = IntOffset(
                            x = 0,
                            y = with(androidx.compose.ui.platform.LocalDensity.current) { 48.dp.roundToPx() }
                        ),
                        properties = PopupProperties(
                            focusable = true,
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true
                        ),
                        onDismissRequest = { showLocation = false }
                    ) {
                        LocationDropdown(onDistrictSelected = { showLocation = false })
                    }
                }
            }

            Spacer(modifier = Modifier.width(5.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = EntryOrange),
                shape = RoundedCornerShape(7.dp),
                contentPadding = PaddingValues(horizontal = 13.dp),
                modifier = Modifier
                    .height(38.dp)
                    .widthIn(min = 142.dp)
            ) {
                Text(
                    text = "Login / Register",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(3.dp))

            IconButton(onClick = {}, modifier = Modifier.size(42.dp)) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* SECOND NAVIGATION BAR                                                      */
/* -------------------------------------------------------------------------- */

@Composable
private fun HomeNavigationBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(EntryDark.copy(alpha = 0.97f))
            .border(width = 1.dp, color = Color(0xFF1D1D1D))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(34.dp)
    ) {
        Text(text = "Turfs", color = EntryOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = "Concerts", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = "Events", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = "Movies", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/* -------------------------------------------------------------------------- */
/* HERO CAROUSEL                                                              */
/* -------------------------------------------------------------------------- */

@Composable
private fun HeroCarousel() {
    val pages = listOf(
        Triple("Weekend Offer", "Play Under The Lights",
            "Book premium 5-a-side football turfs and get 20% off on midnight slots."),
        Triple("New Venues", "Cricket Box Leagues",
            "Discover the best indoor and outdoor box cricket pitches near you.")
    )

    val heroImages = remember { listOf(HERO_IMAGE_1, HERO_IMAGE_2) }
    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentPage = (currentPage + 1) % pages.size
        }
    }

    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(EntryDark)
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(12.dp))
    ) {
        // Lazy-loaded image — only loads when carousel page is visible
        SubcomposeAsyncImage(
            model = heroImages[currentPage],
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            when (painter.state) {
                is coil.compose.AsyncImagePainter.State.Loading -> {
                    Box(Modifier.fillMaxSize().background(EntryDark.copy(alpha = 0.8f)))
                }
                is coil.compose.AsyncImagePainter.State.Error -> {
                    Box(Modifier.fillMaxSize().background(EntryDark))
                }
                else -> {
                    SubcomposeAsyncImageContent(Modifier.fillMaxSize())
                }
            }
        }

        // Dark overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            EntryDark.copy(alpha = 0.92f),
                            EntryDark.copy(alpha = 0.60f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = page.first,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(EntryOrange, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = page.second,
                color = Color.White,
                fontSize = 25.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = page.third,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 285.dp)
            )

            Spacer(modifier = Modifier.height(13.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = EntryOrange),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = if (currentPage == 0) "Book Now" else "Explore Pitches",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(
                            width = if (index == currentPage) 18.dp else 6.dp,
                            height = 6.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) EntryOrange
                            else Color.White.copy(alpha = 0.55f)
                        )
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* TOP RATED VENUES                                                           */
/* -------------------------------------------------------------------------- */

@Composable
private fun TopRatedVenues() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 30.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Top Rated Venues in Coimbatore",
                    color = TextDark,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Book premium sports facilities around your area",
                    color = TextGrey,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "See All", color = EntryOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "See all",
                    tint = EntryOrange,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            turfList.forEach { turf ->
                TurfCard(turf = turf)
            }
        }
    }
}

@Composable
private fun TurfCard(turf: Turf) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFE7E7E7), shape = RoundedCornerShape(12.dp))
            .clickable {}
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            // Smaller image size = faster loading = smoother scroll
            SubcomposeAsyncImage(
                model = turf.image,
                contentDescription = turf.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is coil.compose.AsyncImagePainter.State.Loading -> {
                        Box(Modifier.fillMaxSize().background(Color(0xFFE7E7E7)))
                    }
                    is coil.compose.AsyncImagePainter.State.Error -> {
                        Box(Modifier.fillMaxSize().background(Color(0xFFE7E7E7)))
                    }
                    else -> {
                        SubcomposeAsyncImageContent(Modifier.fillMaxSize())
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFB51B),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = turf.rating, color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = turf.name,
                color = TextDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = EntryOrange,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = turf.location, color = TextGrey, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TurfTag(turf.sport)
                TurfTag(turf.type)
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE9E9E9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = turf.price, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "/hr", color = Color(0xFF9CA3AF), fontSize = 10.sp)
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = EntryOrange),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Book", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TurfTag(text: String) {
    Text(
        text = text,
        color = TextDark,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

/* -------------------------------------------------------------------------- */
/* WEEKEND LEAGUE — blur replaced with cheap gradient                        */
/* -------------------------------------------------------------------------- */

@Composable
private fun WeekendLeagueBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 34.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(EntryDark)
    ) {
        // Cheap ambient glow — replaces expensive blur(38.dp)
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 85.dp, y = (-75).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                EntryOrange.copy(alpha = 0.30f),
                                EntryOrange.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-80).dp, y = 75.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                EntryOrange.copy(alpha = 0.24f),
                                EntryOrange.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOURNAMENTS",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(EntryOrange, RoundedCornerShape(5.dp))
                    .padding(horizontal = 12.dp, vertical = 0.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Join the Weekend League",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Compete with local teams and win exciting prizes!",
                color = Color(0xFFD1D5DB),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = EntryOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = "Register Team",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* SPORTS                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun BrowseSports() {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
        Text(
            text = "Browse by Sports",
            color = TextDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(18.dp))

        val sports = listOf(
            Triple("Football", "24 Venues", Icons.Default.SportsSoccer),
            Triple("Cricket", "18 Venues", Icons.Default.SportsCricket),
            Triple("Badminton", "32 Venues", Icons.Default.SportsBaseball),
            Triple("Tennis", "9 Venues", Icons.Default.SportsTennis),
            Triple("Basketball", "14 Venues", Icons.Default.SportsBasketball)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            sports.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { sport ->
                        SportCard(
                            name = sport.first,
                            venues = sport.second,
                            icon = sport.third,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SportCard(
    name: String,
    venues: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAFAFA))
            .border(width = 1.dp, color = Color(0xFFEAEAEA), shape = RoundedCornerShape(16.dp))
            .padding(vertical = 18.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(width = 1.dp, color = Color(0xFFE8E8E8), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = name, tint = EntryOrange, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = name, color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = venues, color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

/* -------------------------------------------------------------------------- */
/* FOOTER                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun HomeFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EntryDark)
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 40.dp)
    ) {
        Text(
            text = "List your Turf or Sports Venue",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Are you a venue owner? Partner with us to increase your bookings instantly.",
            color = Color(0xFFD1D5DB),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = EntryOrange),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(42.dp)
        ) {
            Text(text = "Partner With Us", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* MOBILE BOTTOM NAVIGATION                                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun MobileBottomNavigation(
    selectedItem: Int,
    items: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>,
    onItemSelected: (Int) -> Unit
) {
    Surface(color = Color.White, shadowElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = selectedItem == index
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onItemSelected(index) }
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.second,
                        contentDescription = item.first,
                        tint = if (selected) EntryOrange else Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.first,
                        color = if (selected) EntryOrange else Color(0xFF6B7280),
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationDropdown(onDistrictSelected: (String) -> Unit) {
    val districts = remember { listOf("Ariyalur","Chengalpattu","Chennai","Coimbatore","Cuddalore","Dharmapuri") }
    var searchText by remember { mutableStateOf("") }
    val filteredDistricts = remember(searchText) {
        districts.filter { it.contains(searchText, ignoreCase = true) }
    }

    Card(
        modifier = Modifier.width(224.dp).wrapContentHeight(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F8F8))
                    .padding(10.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = {
                        Text(text = "Search district...", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EntryOrange,
                        unfocusedBorderColor = EntryOrange.copy(alpha = 0.75f)
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )
            }
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                filteredDistricts.forEach { district ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDistrictSelected(district) }
                            .background(if (district == "Coimbatore") EntryOrange.copy(alpha = 0.07f) else Color.White)
                            .padding(horizontal = 16.dp, vertical = 15.dp)
                    ) {
                        Text(
                            text = district,
                            color = if (district == "Coimbatore") EntryOrange else TextDark,
                            fontSize = 15.sp,
                            fontWeight = if (district == "Coimbatore") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}