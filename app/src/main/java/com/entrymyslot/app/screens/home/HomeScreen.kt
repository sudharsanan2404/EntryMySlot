package com.entrymyslot.app.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.entrymyslot.app.core.components.*
import com.entrymyslot.app.core.utils.LocationHelper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ------------------------------------------------------------
// COLORS
// ------------------------------------------------------------

private val EntryBlueCard = Color(0xFF1648D5).copy(alpha = 0.18f)
private val EntryBlueLight = Color(0xFF1648D5)
private val EntryOrange = EntryCardAccent
private val EntryWhite = Color(0xFFFFFFFF)
private val EntryGray = Color(0xFF98A2B3)
private val EntryDark = Color(0xFF0E0B38).copy(alpha = 0.68f)


private fun getShortCityName(city: String): String = when (city) {
    "Chennai" -> "CHN"
    "Coimbatore" -> "CBE"
    "Madurai" -> "MDU"
    "Tiruchirappalli", "Trichy" -> "TRY"
    "Salem" -> "SLM"
    "Erode" -> "ERD"
    "Vellore" -> "VEL"
    "Tirunelveli" -> "TNV"
    "Thoothukudi" -> "TUT"
    "Ariyalur" -> "ALU"
    "Chengalpattu" -> "CGL"
    "Cuddalore" -> "CUD"
    "Dharmapuri" -> "DPI"
    "Dindigul" -> "DGL"
    "Kallakurichi" -> "KKI"
    "Kancheepuram" -> "KPM"
    "Kanniyakumari" -> "KK"
    "Karur" -> "KRR"
    "Krishnagiri" -> "KGI"
    "Nagapattinam" -> "NGT"
    "Namakkal" -> "NKL"
    "Nilgiris" -> "NIL"
    "Perambalur" -> "PBL"
    "Pudukkottai" -> "PDK"
    "Ramanathapuram" -> "RAM"
    "Ranipet" -> "RPT"
    "Sivaganga" -> "SVG"
    "Tenkasi" -> "TKS"
    "Thanjavur" -> "TNJ"
    "Theni" -> "TNI"
    "Tirupathur" -> "TPT"
    "Tiruppur" -> "TPR"
    "Tiruvallur" -> "TLR"
    "Tiruvannamalai" -> "TVM"
    "Tiruvarur" -> "TVR"
    "Viluppuram" -> "VPM"
    "Virudhunagar" -> "VNR"
    else -> if (city.length > 3) city.take(3).uppercase() else city.uppercase()
}

// ------------------------------------------------------------
// DATA
// ------------------------------------------------------------

data class Glow(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val alpha: Float
)

data class CategoryItem(
    val title: String,
    val icon: ImageVector
)

data class PopularEvent(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val price: String,
    val imageUrl: String? = null
)

private val categories = listOf(
    CategoryItem("Movies", Icons.Outlined.ConfirmationNumber),
    CategoryItem("Sports", Icons.Outlined.SportsSoccer),
    CategoryItem("Events", Icons.Outlined.Event)
)

val popularEvents = listOf(
    PopularEvent(
        id = "pop_1",
        title = "IND vs AUS - 2nd ODI",
        date = "20 Oct 2024 | 2:00 PM",
        location = "Eden Gardens, Kolkata",
        price = "From ₹299"
    ),
    PopularEvent(
        id = "pop_2",
        title = "Arijit Singh Live",
        date = "25 Nov 2024 | 7:00 PM",
        location = "DY Patil Stadium, Mumbai",
        price = "From ₹799"
    ),
    PopularEvent(
        id = "pop_3",
        title = "Live Music Night",
        date = "12 Dec 2024 | 8:00 PM",
        location = "Chennai",
        price = "From ₹499"
    ),
    PopularEvent(
        id = "pop_4",
        title = "Tech Summit 2024",
        date = "15 Jan 2025 | 10:00 AM",
        location = "Trade Center, Bangalore",
        price = "Free Entry"
    ),
    PopularEvent(
        id = "pop_5",
        title = "Stand-up Comedy",
        date = "05 Dec 2024 | 9:00 PM",
        location = "The Laugh Club, Chennai",
        price = "From ₹350"
    ),
    PopularEvent(
        id = "pop_6",
        title = "Food Festival",
        date = "10 Nov 2024 | 11:00 AM",
        location = "Island Ground, Chennai",
        price = "From ₹100"
    )
)

val latestMovies = listOf(
    PopularEvent(
        id = "mov_1",
        title = "The Dark Knight",
        date = "In Cinemas Now",
        location = "IMAX, Chennai",
        price = "From ₹190"
    ),
    PopularEvent(
        id = "mov_2",
        title = "Inception",
        date = "Re-releasing Soon",
        location = "PVR, Bangalore",
        price = "From ₹250"
    ),
    PopularEvent(
        id = "mov_3",
        title = "Interstellar",
        date = "15 Oct 2024",
        location = "Luxe, Mumbai",
        price = "From ₹300"
    ),
    PopularEvent(
        id = "mov_4",
        title = "Avatar: Way of Water",
        date = "In Cinemas Now",
        location = "PVR, Chennai",
        price = "From ₹220"
    ),
    PopularEvent(
        id = "mov_5",
        title = "The Matrix",
        date = "Next Week",
        location = "Sathyam, Chennai",
        price = "From ₹180"
    ),
    PopularEvent(
        id = "mov_6",
        title = "Avengers: Endgame",
        date = "20 Oct 2024",
        location = "INOX, Madurai",
        price = "From ₹150"
    )
)

val sportsNearYou = listOf(
    PopularEvent(
        id = "sport_1",
        title = "Green Arena Turf",
        date = "Open Now",
        location = "Adyar, Chennai",
        price = "From ₹800"
    ),
    PopularEvent(
        id = "sport_2",
        title = "Blue Wave Pool",
        date = "6:00 AM - 9:00 PM",
        location = "Velachery, Chennai",
        price = "From ₹200"
    ),
    PopularEvent(
        id = "sport_3",
        title = "Elite Badminton Club",
        date = "Available Today",
        location = "T. Nagar, Chennai",
        price = "From ₹400"
    ),
    PopularEvent(
        id = "sport_4",
        title = "Victory Cricket Ground",
        date = "Slots Available",
        location = "OMR, Chennai",
        price = "From ₹1500"
    ),
    PopularEvent(
        id = "sport_5",
        title = "Smash Tennis Court",
        date = "Open 24/7",
        location = "Anna Nagar, Chennai",
        price = "From ₹600"
    ),
    PopularEvent(
        id = "sport_6",
        title = "Dunk Basket Court",
        date = "Available Now",
        location = "Porur, Chennai",
        price = "From ₹300"
    )
)

// ------------------------------------------------------------
// GLOW BACKGROUND
// ------------------------------------------------------------

@Composable
fun GlowBackground(
    modifier: Modifier = Modifier
) {
    val glows = listOf(
        Glow(
            x = 0.50f,
            y = 0.02f,
            radius = 0.62f,
            color = Color(0xFF0E0B38),
            alpha = 0.30f
        ),
        Glow(
            x = 0.08f,
            y = 0.10f,
            radius = 0.52f,
            color = Color(0xFF1648D5),
            alpha = 0.22f
        ),
        Glow(
            x = 0.92f,
            y = 0.14f,
            radius = 0.60f,
            color = Color(0xFF000000),
            alpha = 0.28f
        ),
        Glow(
            x = 0.52f,
            y = 0.30f,
            radius = 0.52f,
            color = Color(0xFF0A3BC2),
            alpha = 0.16f
        ),
        Glow(
            x = 0.90f,
            y = 0.43f,
            radius = 0.55f,
            color = Color(0xFF0739B8),
            alpha = 0.16f
        ),
        Glow(
            x = 0.02f,
            y = 0.47f,
            radius = 0.48f,
            color = Color(0xFF082C94),
            alpha = 0.10f
        ),
        Glow(
            x = 0.55f,
            y = 0.62f,
            radius = 0.55f,
            color = Color(0xFF062E8F),
            alpha = 0.10f
        ),
        Glow(
            x = 0.85f,
            y = 0.76f,
            radius = 0.52f,
            color = Color(0xFF082A82),
            alpha = 0.08f
        ),
        Glow(
            x = 0.00f,
            y = 0.88f,
            radius = 0.65f,
            color = Color(0xFF020E2D),
            alpha = 0.30f
        ),
        Glow(
            x = 0.48f,
            y = 1.05f,
            radius = 0.60f,
            color = Color(0xFF020F31),
            alpha = 0.22f
        )
    )

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF092E9A),
                    Color(0xFF082A82),
                    Color(0xFF072364),
                    Color(0xFF061D4E),
                    Color(0xFF061A40),
                    Color(0xFF061A3D)
                ),
                startY = 0f,
                endY = size.height
            )
        )

        glows.forEach { glow ->
            drawSoftGlow(glow = glow)
        }
        
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color(0xFF020B20).copy(alpha = 0.04f),
                    Color(0xFF020B20).copy(alpha = 0.10f)
                ),
                startY = size.height * 0.45f,
                endY = size.height
            )
        )
    }
}

private fun DrawScope.drawSoftGlow(
    glow: Glow
) {
    val center = Offset(
        x = size.width * glow.x,
        y = size.height * glow.y
    )
    val radius = size.maxDimension * glow.radius

    drawCircle(
        center = center,
        radius = radius,
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to glow.color.copy(alpha = glow.alpha),
                0.25f to glow.color.copy(alpha = glow.alpha * 0.70f),
                0.50f to glow.color.copy(alpha = glow.alpha * 0.35f),
                0.72f to glow.color.copy(alpha = glow.alpha * 0.10f),
                1.0f to Color.Transparent
            ),
            center = center,
            radius = radius,
            tileMode = TileMode.Clamp
        )
    )
}

// ------------------------------------------------------------
// HOME SCREEN
// ------------------------------------------------------------

@Composable
fun HomeScreen(
    onCategoryClick: (String) -> Unit = {},
    onEventClick: (PopularEvent) -> Unit = {},
    onBottomNavigationClick: (String) -> Unit = {},
    onSportClick: (PopularEvent) -> Unit = {},
    onMovieBookClick: (PopularEvent) -> Unit = {},
    onSearchClick: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel()
) {
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedBottomItem by remember {
        mutableStateOf("Home")
    }

    var favoriteEvents by remember {
        mutableStateOf(setOf<String>())
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0E0B38),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Spacer(Modifier.height(48.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        "EntryMySlot",
                        color = EntryWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Your ultimate booking companion",
                        color = EntryGray,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(32.dp))

                NavigationDrawerItem(
                    label = { Text("Profile", color = EntryWhite) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onBottomNavigationClick("Profile")
                    },
                    icon = { Icon(Icons.Outlined.AccountCircle, null, tint = EntryOrange) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("My Bookings", color = EntryWhite) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onBottomNavigationClick("My Bookings")
                    },
                    icon = { Icon(Icons.Outlined.ConfirmationNumber, null, tint = EntryOrange) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("Notifications", color = EntryWhite) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Outlined.Notifications, null, tint = EntryOrange) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("Settings", color = EntryWhite) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Outlined.Settings, null, tint = EntryOrange) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
            }
        }
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
                // Actual content
                HomeContent(
                    favoriteEvents = favoriteEvents,
                    onFavoriteClick = { id ->
                        favoriteEvents =
                            if (favoriteEvents.contains(id)) {
                                favoriteEvents - id
                            } else {
                                favoriteEvents + id
                            }
                    },
                    onCategoryClick = onCategoryClick,
                    onEventClick = onMovieBookClick,
                    onSportClick = onSportClick,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onNotificationClick = { /* Handle notification click */ },
                    featuredEvents = homeState.events.map { it.toPopularEvent() }.ifEmpty { popularEvents },
                    featuredMovies = homeState.movies.map { it.toPopularEvent() }.ifEmpty { latestMovies },
                    nearbySports = homeState.sports.map { it.toPopularEvent() }.ifEmpty { sportsNearYou },
                    isLoading = homeState.isLoading,
                    loadError = homeState.error,
                    selectedCity = homeState.selectedCity,
                    onCityChanged = { homeViewModel.updateCity(it) },
                    onRetry = homeViewModel::refresh,
                    onSearchClick = onSearchClick,
                    modifier = Modifier.weight(1f)
                )

                // ------------------------------------------------
                // BOTTOM NAVIGATION
                // ------------------------------------------------

                HomeBottomNavigation(
                    selectedItem = selectedBottomItem,
                    onItemSelected = {
                        selectedBottomItem = it
                        onBottomNavigationClick(it)
                    }
                )
            }
        }
    }
}



// ------------------------------------------------------------
// HOME CONTENT
// ------------------------------------------------------------

@Composable
private fun HomeContent(
    favoriteEvents: Set<String>,
    onFavoriteClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onEventClick: (PopularEvent) -> Unit,
    onSportClick: (PopularEvent) -> Unit,
    featuredEvents: List<PopularEvent>,
    featuredMovies: List<PopularEvent>,
    nearbySports: List<PopularEvent>,
    isLoading: Boolean,
    loadError: String?,
    selectedCity: String,
    onCityChanged: (String) -> Unit,
    onRetry: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    var showLocationPicker by remember { mutableStateOf(false) }

    val locationFetcher = rememberLocationFetcher { city ->
        onCityChanged(city)
        showLocationPicker = false // Auto-close when detected
    }

    if (locationFetcher.showGpsDialog) {
        GpsDisabledDialog(
            onConfirm = locationFetcher.onOpenLocationSettings,
            onDismiss = locationFetcher.onDismissGpsDialog
        )
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            bottom = 12.dp
        )
    ) {

        // ----------------------------------------------------
        // HEADER
        // ----------------------------------------------------

        item {
            HomeHeader(
                onMenuClick = onMenuClick,
                onNotificationClick = onNotificationClick
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
//
//        if (isLoading || loadError != null) {
//            item { HomeLoadStatus(isLoading, loadError, onRetry) }
//        }

        // ----------------------------------------------------
        // SEARCH
        // ----------------------------------------------------

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
        SearchBar(onClick = onSearchClick)
                }

                // Location Button
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(40.dp)
                        .clickable {
                            showLocationPicker = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(EntryOrange)
                            .height(45.dp)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (locationFetcher.state is LocationFetchState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = getShortCityName(selectedCity),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select location",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // ----------------------------------------------------
        // PROMOTIONAL BANNER
        // ----------------------------------------------------

        item {
            PromotionalBanner()

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }

        // ----------------------------------------------------
        // CATEGORIES
        // ----------------------------------------------------

        item {
            Column {
                SectionTitle(
                    title = "Categories"
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(categories, key = { it.title }) { category ->

                        CategoryCard(
                            category = category,
                            onClick = {
                                onCategoryClick(category.title)
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )
        }

        // ----------------------------------------------------
        // POPULAR EVENTS TITLE
        // ----------------------------------------------------

        item {
            SectionHeader(
                title = "Popular Events",
                onSeeAllClick = { onCategoryClick("Popular Events") }
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )
        }

        // ----------------------------------------------------
        // POPULAR EVENTS
        // ----------------------------------------------------

        item {
            EventRow(
                events = featuredEvents,
                favoriteEvents = favoriteEvents,
                onFavoriteClick = onFavoriteClick,
                onEventClick = onEventClick
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )
        }

        // ----------------------------------------------------
        // LATEST MOVIES
        // ----------------------------------------------------

        item {
            Column {
                SectionHeader(
                    title = "Latest Movies",
                    onSeeAllClick = { onCategoryClick("Latest Movies") }
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                EventRow(
                    events = featuredMovies,
                    favoriteEvents = favoriteEvents,
                    onFavoriteClick = onFavoriteClick,
                    onEventClick = onEventClick
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )
        }

        // ----------------------------------------------------
        // SPORTS NEAR YOU
        // ----------------------------------------------------

        item {
            Column {
                SectionHeader(
                    title = "Sports Near You",
                    onSeeAllClick = { onCategoryClick("Sports Near You") }
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                EventRow(
                    events = nearbySports,
                    favoriteEvents = favoriteEvents,
                    onFavoriteClick = onFavoriteClick,
                    onEventClick = onSportClick
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
    // Dialog-ah open panra logic
    if (showLocationPicker) {
        ProfessionalLocationPicker(
            locationFetcher = locationFetcher,
            onDismiss = { showLocationPicker = false },
            onCitySelected = { city ->
                onCityChanged(city)
                showLocationPicker = false
            }
        )
    }
}


// ------------------------------------------------------------
// HEADER
// ------------------------------------------------------------

@Composable
private fun HomeHeader(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Menu",
                tint = EntryWhite
            )
        }

        Image(
            painter = painterResource(id = R.drawable.entrymyslotlogopcg),
            contentDescription = "EntryMySlot",
            modifier = Modifier
                .width(180.dp)
                .height(60.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )

        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = EntryWhite
            )
        }
    }
}


// ------------------------------------------------------------
// SEARCH BAR
// ------------------------------------------------------------

@Composable
private fun SearchBar(onClick: () -> Unit) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 8.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Search events, movies, sports...",
                color = Color(0xFF686D75),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = Color(0xFF42474E),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun HomeLoadStatus(isLoading: Boolean, error: String?, onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = EntryOrange, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refreshing nearby picks…", color = EntryWhite.copy(alpha = 0.8f), fontSize = 12.sp)
        } else if (error != null) {
            Text("Showing saved picks", color = EntryGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("Retry", color = EntryWhite, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onRetry))
        }
    }
}


// ------------------------------------------------------------
// PROMOTIONAL BANNER
// ------------------------------------------------------------

@Composable
private fun PromotionalBanner() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0E0B38).copy(alpha = .68f))
            .border(
                width = 1.dp,
                color = Color(0xFF1648D5).copy(alpha = 0.38f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // IMAGE PLACEHOLDER (Reference image shows a movie poster style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 500f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {

            Text(
                text = "THE EPIC",
                color = EntryWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "BLOCKBUSTER",
                color = EntryWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "IN CINEMAS NOW",
                color = EntryWhite.copy(alpha = 0.7f),
                fontSize = 11.sp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EntryBlueLight)
                    .clickable { }
                    .padding(
                        horizontal = 20.dp,
                        vertical = 10.dp
                    )
            ) {

                Text(
                    text = "BOOK NOW",
                    color = EntryWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Simulating the person image on the right
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(180.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "IMAGE",
                color = Color(0xFF1648D5),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ------------------------------------------------------------
// SECTION TITLE
// ------------------------------------------------------------

@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        color = EntryWhite,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = EntryWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "See All",
            color = EntryWhite,
            fontSize = 13.sp,
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
}

@Composable
private fun EventRow(
    events: List<PopularEvent>,
    favoriteEvents: Set<String>,
    onFavoriteClick: (String) -> Unit,
    onEventClick: (PopularEvent) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(
            horizontal = 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = events,
            key = { it.id }
        ) { event ->
            PopularEventCard(
                event = event,
                isFavorite = favoriteEvents.contains(event.id),
                onFavoriteClick = { onFavoriteClick(event.id) },
                onClick = { onEventClick(event) }
            )
        }
    }
}

// ------------------------------------------------------------
// CATEGORY CARD
// ------------------------------------------------------------

@Composable
private fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ScaleAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1648D5).copy(alpha = .18f))
                .border(
                    width = 1.dp,
                    color = Color(0xFF1648D5).copy(alpha = 0.38f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                tint = EntryOrange,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = category.title,
            color = EntryWhite.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}


// ------------------------------------------------------------
// POPULAR EVENT CARD
// ------------------------------------------------------------

@Composable
private fun PopularEventCard(
    event: PopularEvent,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ScaleAnimation"
    )

    Column(
        modifier = Modifier
            .width(180.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = .35f),
                spotColor = Color.Black.copy(alpha = .35f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1D2550), Color(0xFF171E42))))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = .06f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp
                        )
                    )
                    .background(Color(0xFF1648D5).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                if (event.imageUrl != null) {
                    coil3.compose.AsyncImage(
                        model = event.imageUrl,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "IMAGE",
                        color = EntryGray.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Color.Black.copy(alpha = 0.3f)
                    )
                    .clickable {
                        onFavoriteClick()
                    },
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Rounded.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = "Favorite",
                    tint = if (isFavorite) {
                        Color.Red
                    } else {
                        EntryWhite
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(top = 10.dp)) {

            Text(
                text = event.title,
                color = EntryWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = event.date,
                color = EntryGray,
                fontSize = 12.sp,
                maxLines = 1
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = event.location,
                color = EntryGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = event.price,
                color = EntryOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun com.entrymyslot.app.data.model.HomeContent.toPopularEvent() = PopularEvent(
    id = id,
    title = title,
    date = date,
    location = location,
    price = price,
    imageUrl = imageUrl
)


// ------------------------------------------------------------
// BOTTOM NAVIGATION
// ------------------------------------------------------------

@Composable
private fun HomeBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {

    val items = listOf(
        Triple("Home", Icons.Outlined.Home, Icons.Rounded.Home),
        Triple("Search", Icons.Outlined.Search, Icons.Outlined.Search),
        Triple("My Bookings", Icons.Outlined.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        Triple("Profile", Icons.Outlined.AccountCircle, Icons.Outlined.AccountCircle)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0E0B38).copy(alpha = .86f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color(0xFF1648D5).copy(alpha = .38f)),
            shadowElevation = 10.dp
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                items.forEach { item ->

                    val selected = selectedItem == item.first

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) EntryOrange.copy(alpha = .16f) else Color.Transparent)
                            .clickable {
                                onItemSelected(item.first)
                            }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = if (selected) item.third else item.second,
                            contentDescription = item.first,
                            tint = if (selected) {
                                EntryOrange
                            } else {
                                EntryGray
                            },
                            modifier = Modifier.size(19.dp)
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = item.first,
                            color = if (selected) {
                                EntryOrange
                            } else {
                                EntryGray
                            },
                            fontSize = 8.sp,
                            fontWeight = if (selected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalLocationPicker(
    locationFetcher: LocationFetcherController,
    onDismiss: () -> Unit,
    onCitySelected: (String) -> Unit
) {
    val darkBlueBg = Color(0xFF0A1D4D) // EntryDark
    val cardBlue = Color(0xFF102868) // Darker variant for internal boxes
    val primaryOrange = Color(0xFFFF8A00) // EntryOrange
    val textGray = Color(0xFF98A2B3) // EntryGray
    val borderColor = Color(0xFF1E3A8A).copy(alpha = 0.5f)

    var searchQuery by remember { mutableStateOf("") }
    val districts = listOf(
        "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore",
        "Dharmapuri", "Dindigul", "Erode", "Kallakurichi", "Kancheepuram",
        "Kanniyakumari", "Karur", "Krishnagiri", "Madurai", "Mayiladuthurai",
        "Nagapattinam", "Namakkal", "Nilgiris", "Perambalur", "Pudukkottai",
        "Ramanathapuram", "Ranipet", "Salem", "Sivaganga", "Tenkasi",
        "Thanjavur", "Theni", "Thoothukudi", "Tiruchirappalli", "Tirunelveli",
        "Tirupathur", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur",
        "Vellore", "Viluppuram", "Virudhunagar"
    )

    val filteredDistricts = districts.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = darkBlueBg),
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Location",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find events and venues near you",
                            color = textGray,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Detection UI
                val fetchState = locationFetcher.state
                if (fetchState is LocationFetchState.Success) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0056FF).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF0056FF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF0056FF), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Current location identified",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = fetchState.cityName,
                                    color = Color(0xFF0056FF),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "IN USE",
                                color = Color(0xFF0056FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { locationFetcher.onStart() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryOrange),
                        enabled = fetchState !is LocationFetchState.Loading
                    ) {
                        if (fetchState is LocationFetchState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Use my current location",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(borderColor))
                    Text(
                        text = "CHOOSE MANUALLY",
                        color = textGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(borderColor))
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search city or district...",
                            color = textGray.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = textGray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryOrange,
                        unfocusedBorderColor = borderColor,
                        cursorColor = primaryOrange,
                        focusedContainerColor = cardBlue,
                        unfocusedContainerColor = cardBlue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp) // Reduced height
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBlue)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                ) {
                    LazyColumn {
                        items(filteredDistricts, key = { it }) { district ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onCitySelected(district) }
                                        .padding(horizontal = 20.dp, vertical = 15.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = district,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = getShortCityName(district),
                                        color = textGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (district != filteredDistricts.last()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(borderColor.copy(alpha = 0.2f))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = textGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Manual selection is stored on device.",
                        color = textGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
