package com.entrymyslot.app.screens.home

import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.entrymyslot.app.screens.movies.MovieOverviewContent

// ------------------------------------------------------------
// COLORS
// ------------------------------------------------------------

private val EntryBlueCard = Color(0xFF0A1D4D)
private val EntryBlueLight = Color(0xFF0056FF)
private val EntryOrange = Color(0xFFFF8A00)
private val EntryWhite = Color(0xFFFFFFFF)
private val EntryGray = Color(0xFF98A2B3)
private val EntryDark = Color(0xFF0A1D4D)


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
    CategoryItem("Events", Icons.Outlined.Event),
    CategoryItem("Concerts", Icons.Outlined.MusicNote),
    CategoryItem("More", Icons.Filled.GridView)
)

private val popularEvents = listOf(
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
    )
)

private val latestMovies = listOf(
    PopularEvent(
        id = "mov_1",
        title = "The Dark Knight",
        date = "In Cinemas Now",
        location = "IMAX, Chennai",
        price = "From ₹199"
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
    )
)

private val sportsNearYou = listOf(
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
    onAuthClick: () -> Unit = {},
    onSportClick: (PopularEvent) -> Unit = {},
    onMovieBookClick: (PopularEvent) -> Unit = {}
) {

    val homeViewModel = remember { HomeViewModel() }
    val homeState by homeViewModel.uiState.collectAsState()

    var selectedBottomItem by remember {
        mutableStateOf("Home")
    }

    var favoriteEvents by remember {
        mutableStateOf(setOf<String>())
    }

    var showMovieOverview by remember { mutableStateOf(false) }
    var selectedMovie by remember { mutableStateOf<PopularEvent?>(null) }

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
                onEventClick = { event ->
                    selectedMovie = event
                    showMovieOverview = true
                },
                onAuthClick = onAuthClick,
                onSportClick = onSportClick,
                featuredEvents = homeState.events.map { it.toPopularEvent() }.ifEmpty { popularEvents },
                featuredMovies = homeState.movies.map { it.toPopularEvent() }.ifEmpty { latestMovies },
                nearbySports = homeState.sports.map { it.toPopularEvent() }.ifEmpty { sportsNearYou },
                isLoading = homeState.isLoading,
                loadError = homeState.error,
                onRetry = homeViewModel::refresh,
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

        // ----------------------------------------------------
        // MOVIE OVERVIEW BOTTOM SHEET
        // ----------------------------------------------------
        if (showMovieOverview && selectedMovie != null) {
            MovieOverviewBottomSheet(
                movie = selectedMovie!!,
                onDismiss = { showMovieOverview = false },
                onBookClick = {
                    showMovieOverview = false
                    onMovieBookClick(selectedMovie!!)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieOverviewBottomSheet(
    movie: PopularEvent,
    onDismiss: () -> Unit,
    onBookClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF101A2C),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        MovieOverviewContent(
            movie = movie,
            onBookClick = onBookClick
        )
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
    onAuthClick: () -> Unit,
    onSportClick: (PopularEvent) -> Unit,
    featuredEvents: List<PopularEvent>,
    featuredMovies: List<PopularEvent>,
    nearbySports: List<PopularEvent>,
    isLoading: Boolean,
    loadError: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {

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
            HomeHeader(onAuthClick = onAuthClick)

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        if (isLoading || loadError != null) {
            item { HomeLoadStatus(isLoading, loadError, onRetry) }
        }

        // ----------------------------------------------------
        // SEARCH
        // ----------------------------------------------------

        item {
            SearchBar()

            Spacer(
                modifier = Modifier.height(20.dp)
            )
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

                    items(categories) { category ->

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
}


// ------------------------------------------------------------
// HEADER
// ------------------------------------------------------------

@Composable
private fun HomeHeader(
    onAuthClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                Image(
                    painter = painterResource(
                        id = R.drawable.entrymyslotlogopcg
                    ),
                    contentDescription = "EntryMySlot",
                    modifier = Modifier
                        .width(180.dp)
                        .height(60.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EntryOrange)
                        .clickable { onAuthClick() }
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "Login/Register",
                        color = EntryWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ------------------------------------------------------------
// SEARCH BAR
// ------------------------------------------------------------

@Composable
private fun SearchBar() {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(44.dp),
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
            .background(Color(0xFF0A1D4D))
            .border(
                width = 1.dp,
                color = Color(0xFF1E3A8A).copy(alpha = 0.5f),
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
                color = Color(0xFF1E3A8A),
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
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF102868), Color(0xFF0A1D4D))
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF1E3A8A).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                tint = if (category.title == "More") {
                    EntryWhite
                } else {
                    EntryOrange
                },
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
            .clip(RoundedCornerShape(16.dp))
            .background(EntryDark)
            .border(
                width = 1.dp,
                color = Color(0xFF1E3A8A).copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
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
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )
                    )
                    .background(Color(0xFF1E3A8A).copy(alpha = 0.2f)),
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

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = event.title,
                color = EntryWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = event.date,
                color = EntryGray,
                fontSize = 10.sp,
                maxLines = 1
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = event.location,
                color = EntryGray,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.height(8.dp)
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

private fun com.entrymyslot.app.data.model.HomeContent.toPopularEvent() = PopularEvent(
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
        Triple("My Bookings", Icons.Outlined.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        Triple("Wallet", Icons.Outlined.Wallet, Icons.Outlined.Wallet),
        Triple("Profile", Icons.Outlined.AccountCircle, Icons.Outlined.AccountCircle)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00227A).copy(alpha = 0.8f),
                        Color(0xFF001242)
                    )
                )
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            tonalElevation = 0.dp
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 12.dp,
                        bottom = WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding() + 8.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                items.forEach { item ->

                    val selected = selectedItem == item.first

                    Column(
                        modifier = Modifier
                            .width(75.dp)
                            .clickable {
                                onItemSelected(item.first)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = if (selected) item.third else item.second,
                            contentDescription = item.first,
                            tint = if (selected) {
                                EntryWhite
                            } else {
                                EntryGray
                            },
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = item.first,
                            color = if (selected) {
                                EntryWhite
                            } else {
                                EntryGray
                            },
                            fontSize = 10.sp,
                            fontWeight = if (selected) {
                                FontWeight.Medium
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
