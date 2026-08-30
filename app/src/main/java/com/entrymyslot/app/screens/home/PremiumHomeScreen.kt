package com.entrymyslot.app.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.R
import com.entrymyslot.app.core.components.GpsDisabledDialog
import com.entrymyslot.app.core.components.LocationFetchState
import com.entrymyslot.app.core.components.rememberLocationFetcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PremiumOrange = Color(0xFFFA580B)
private val PremiumBackground = Color(0xFF061A38)
private val PremiumSurface = Color(0xFF0B274F)
private val PremiumSurfaceRaised = Color(0xFF0E315E)
private val PremiumBlue = Color(0xFF0A2D62)
private val PremiumBlueEdge = Color(0xFF3976A8)
private val PremiumWhite = Color(0xFFF8FAFF)
private val PremiumSecondary = Color(0xFFA8B8CF)
private val PremiumMuted = Color(0xFF7185A1)

private enum class HomeContentKind { Event, Movie, Sport }

private data class PromotionBanner(
    val category: String,
    val title: String,
    val subtitle: String,
    val cta: String,
    val destination: String,
    val icon: ImageVector,
    val startColor: Color,
    val endColor: Color
)

private val promotionBanners = listOf(
    PromotionBanner(
        category = "NOW SHOWING",
        title = "Big-screen stories await",
        subtitle = "Find a showtime and reserve your perfect seats.",
        cta = "Explore movies",
        destination = "Movies",
        icon = Icons.Outlined.ConfirmationNumber,
        startColor = Color(0xFF123F77),
        endColor = Color(0xFF071C3E)
    ),
    PromotionBanner(
        category = "SPORTS NEAR YOU",
        title = "Own the next game",
        subtitle = "Discover nearby venues and book your slot.",
        cta = "Find a venue",
        destination = "Sports",
        icon = Icons.Outlined.SportsSoccer,
        startColor = Color(0xFF16446D),
        endColor = Color(0xFF071D3C)
    ),
    PromotionBanner(
        category = "LIVE EXPERIENCES",
        title = "Make tonight memorable",
        subtitle = "Browse events worth stepping out for.",
        cta = "View events",
        destination = "Events",
        icon = Icons.Outlined.Event,
        startColor = Color(0xFF263D72),
        endColor = Color(0xFF091B3B)
    )
)

@Composable
internal fun PremiumHomeScreen(
    featuredEvents: List<PopularEvent>,
    featuredMovies: List<PopularEvent>,
    nearbySports: List<PopularEvent>,
    selectedCity: String,
    onCategoryClick: (String) -> Unit,
    onEventClick: (PopularEvent) -> Unit,
    onBottomNavigationClick: (String) -> Unit,
    onSportClick: (PopularEvent) -> Unit,
    onMovieBookClick: (PopularEvent) -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedBottomItem by remember { mutableStateOf("Home") }
    var favoriteEvents by remember { mutableStateOf(setOf<String>()) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PremiumDrawer(
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    onBottomNavigationClick("Profile")
                },
                onBookingsClick = {
                    scope.launch { drawerState.close() }
                    onBottomNavigationClick("My Bookings")
                },
                onNotificationsClick = { scope.launch { drawerState.close() } },
                onSettingsClick = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GlowBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                PremiumHomeContent(
                    featuredEvents = featuredEvents,
                    featuredMovies = featuredMovies,
                    nearbySports = nearbySports,
                    favoriteEvents = favoriteEvents,
                    selectedCity = selectedCity,
                    onFavoriteClick = { id ->
                        favoriteEvents = if (favoriteEvents.contains(id)) {
                            favoriteEvents - id
                        } else {
                            favoriteEvents + id
                        }
                    },
                    onCategoryClick = onCategoryClick,
                    onEventClick = onEventClick,
                    onSportClick = onSportClick,
                    onMovieBookClick = onMovieBookClick,
                    onSearchClick = onSearchClick,
                    onLocationClick = onLocationClick,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier.weight(1f)
                )

                PremiumBottomNavigation(
                    selectedItem = selectedBottomItem,
                    onItemSelected = { item ->
                        selectedBottomItem = item
                        onBottomNavigationClick(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun PremiumHomeContent(
    featuredEvents: List<PopularEvent>,
    featuredMovies: List<PopularEvent>,
    nearbySports: List<PopularEvent>,
    favoriteEvents: Set<String>,
    selectedCity: String,
    onFavoriteClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onEventClick: (PopularEvent) -> Unit,
    onSportClick: (PopularEvent) -> Unit,
    onMovieBookClick: (PopularEvent) -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item(key = "header") {
            PremiumHomeHeader(
                onMenuClick = onMenuClick,
                onNotificationClick = { }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item(key = "search_location") {
            SearchLocationToolbar(
                selectedCity = selectedCity,
                onSearchClick = onSearchClick,
                onLocationClick = onLocationClick
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        item(key = "promotions") {
            PromotionalCarousel(onBannerClick = onCategoryClick)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item(key = "categories") {
            PremiumSectionTitle(text = "Categories")
            Spacer(modifier = Modifier.height(12.dp))
            CategorySection(onCategoryClick = onCategoryClick)
            Spacer(modifier = Modifier.height(28.dp))
        }

        item(key = "popular_events") {
            ContentSection(
                title = "Popular Events",
                events = featuredEvents,
                kind = HomeContentKind.Event,
                favoriteEvents = favoriteEvents,
                onFavoriteClick = onFavoriteClick,
                onSeeAllClick = { onCategoryClick("Popular Events") },
                onEventClick = onEventClick
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        item(key = "latest_movies") {
            ContentSection(
                title = "Latest Movies",
                events = featuredMovies,
                kind = HomeContentKind.Movie,
                favoriteEvents = favoriteEvents,
                onFavoriteClick = onFavoriteClick,
                onSeeAllClick = { onCategoryClick("Latest Movies") },
                onEventClick = onMovieBookClick
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        item(key = "sports_near_you") {
            ContentSection(
                title = "Sports Near You",
                events = nearbySports,
                kind = HomeContentKind.Sport,
                favoriteEvents = favoriteEvents,
                onFavoriteClick = onFavoriteClick,
                onSeeAllClick = { onCategoryClick("Sports Near You") },
                onEventClick = onSportClick
            )
        }
    }
}

@Composable
private fun PremiumHomeHeader(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 14.dp)
    ) {
        PremiumIconButton(
            icon = Icons.Outlined.Menu,
            contentDescription = "Menu",
            onClick = onMenuClick,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.entrymyslotlogopcg),
            contentDescription = "EntryMySlot",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .width(152.dp)
                .height(45.dp)
        )
        PremiumIconButton(
            icon = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            onClick = onNotificationClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun PremiumIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "headerIconScale"
    )
    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(PremiumBlue.copy(alpha = 0.82f))
            .border(1.dp, Color.White.copy(alpha = 0.09f), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = PremiumWhite,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SearchLocationToolbar(
    selectedCity: String,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val searchInteraction = remember { MutableInteractionSource() }
        val searchPressed by searchInteraction.collectIsPressedAsState()
        val searchScale by animateFloatAsState(
            targetValue = if (searchPressed) 0.99f else 1f,
            animationSpec = tween(100),
            label = "searchScale"
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .graphicsLayer { scaleX = searchScale; scaleY = searchScale }
                .clip(RoundedCornerShape(15.dp))
                .background(PremiumSurface.copy(alpha = 0.96f))
                .border(
                    1.dp,
                    PremiumBlueEdge.copy(alpha = 0.28f),
                    RoundedCornerShape(15.dp)
                )
                .clickable(
                    interactionSource = searchInteraction,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = "Search events, movies, and sports",
                    onClick = onSearchClick
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = PremiumSecondary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = "Search movies, sports, events",
                color = PremiumSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        LocationChip(
            city = selectedCity,
            onClick = onLocationClick
        )
    }
}

@Composable
private fun LocationChip(city: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(100),
        label = "locationScale"
    )
    Row(
        modifier = Modifier
            .height(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(15.dp))
            .background(PremiumOrange)
            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(15.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Select location",
                onClick = onClick
            )
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = premiumShortCityName(city),
            color = PremiumWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(3.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Select location",
            tint = PremiumWhite,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PromotionalCarousel(onBannerClick: (String) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { promotionBanners.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(5_000)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    page = (pagerState.currentPage + 1) % promotionBanners.size,
                    animationSpec = tween(520)
                )
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            PromotionalBannerCard(
                banner = promotionBanners[page],
                onClick = { onBannerClick(promotionBanners[page].destination) }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            promotionBanners.indices.forEach { index ->
                val selected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (selected) 22.dp else 7.dp,
                    animationSpec = tween(180),
                    label = "bannerIndicatorWidth"
                )
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(7.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) PremiumOrange
                            else PremiumSecondary.copy(alpha = 0.32f)
                        )
                )
            }
        }
    }
}

@Composable
private fun PromotionalBannerCard(
    banner: PromotionBanner,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .shadow(
                elevation = 7.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(banner.startColor, banner.endColor)))
            .border(
                1.dp,
                PremiumBlueEdge.copy(alpha = 0.3f),
                RoundedCornerShape(22.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 38.dp)
                .clip(CircleShape)
                .background(PremiumBlueEdge.copy(alpha = 0.09f))
        )
        Icon(
            imageVector = banner.icon,
            contentDescription = null,
            tint = PremiumWhite.copy(alpha = 0.12f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp)
                .size(92.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.74f)
                .padding(18.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = banner.category,
                color = PremiumOrange,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = banner.title,
                color = PremiumWhite,
                fontSize = 20.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = banner.subtitle,
                color = PremiumSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(11.dp))
            BannerAction(text = banner.cta, onClick = onClick)
        }
    }
}

@Composable
private fun BannerAction(text: String, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(90),
        label = "bannerActionScale"
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(10.dp))
            .background(PremiumOrange)
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = PremiumWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PremiumSectionTitle(text: String) {
    Text(
        text = text,
        color = PremiumWhite,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .semantics { heading() }
    )
}

@Composable
private fun CategorySection(onCategoryClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CategoryTile(
            title = "Movies",
            subtitle = "Cinema",
            icon = Icons.Outlined.ConfirmationNumber,
            onClick = { onCategoryClick("Movies") },
            modifier = Modifier.weight(1f)
        )
        CategoryTile(
            title = "Sports",
            subtitle = "Play",
            icon = Icons.Outlined.SportsSoccer,
            onClick = { onCategoryClick("Sports") },
            modifier = Modifier.weight(1f)
        )
        CategoryTile(
            title = "Events",
            subtitle = "Live",
            icon = Icons.Outlined.Event,
            onClick = { onCategoryClick("Events") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CategoryTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "categoryScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 1.dp else 4.dp,
        animationSpec = tween(110),
        label = "categoryElevation"
    )
    Column(
        modifier = modifier
            .height(112.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(PremiumSurface.copy(alpha = 0.96f))
            .border(
                1.dp,
                PremiumBlueEdge.copy(alpha = 0.27f),
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PremiumOrange.copy(alpha = 0.12f))
                .border(
                    1.dp,
                    PremiumOrange.copy(alpha = 0.22f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PremiumOrange,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = PremiumWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = subtitle,
            color = PremiumMuted,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun ContentSection(
    title: String,
    events: List<PopularEvent>,
    kind: HomeContentKind,
    favoriteEvents: Set<String>,
    onFavoriteClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onEventClick: (PopularEvent) -> Unit
) {
    PremiumSectionHeader(title = title, onSeeAllClick = onSeeAllClick)
    Spacer(modifier = Modifier.height(12.dp))
    PremiumContentRow(
        events = events,
        kind = kind,
        favoriteEvents = favoriteEvents,
        onFavoriteClick = onFavoriteClick,
        onEventClick = onEventClick
    )
}

@Composable
private fun PremiumSectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PremiumWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.weight(1f))
        val source = remember { MutableInteractionSource() }
        val pressed by source.collectIsPressedAsState()
        val color by animateColorAsState(
            targetValue = if (pressed) PremiumOrange else PremiumSecondary,
            animationSpec = tween(100),
            label = "seeAllColor"
        )
        Text(
            text = "See All",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .clickable(
                    interactionSource = source,
                    indication = null,
                    role = Role.Button,
                    onClick = onSeeAllClick
                )
                .padding(horizontal = 9.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PremiumContentRow(
    events: List<PopularEvent>,
    kind: HomeContentKind,
    favoriteEvents: Set<String>,
    onFavoriteClick: (String) -> Unit,
    onEventClick: (PopularEvent) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cardWidth = ((maxWidth - 50.dp) / 2.15f).coerceIn(124.dp, 174.dp)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = events, key = { event -> event.id }) { event ->
                PremiumContentCard(
                    event = event,
                    kind = kind,
                    cardWidth = cardWidth,
                    isFavorite = favoriteEvents.contains(event.id),
                    onFavoriteClick = { onFavoriteClick(event.id) },
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}

@Composable
private fun PremiumContentCard(
    event: PopularEvent,
    kind: HomeContentKind,
    cardWidth: Dp,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(100),
        label = "contentCardScale"
    )
    val imageHeight = if (kind == HomeContentKind.Movie) cardWidth * 1.42f else cardWidth * 0.68f

    Column(
        modifier = Modifier
            .width(cardWidth)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = if (pressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(17.dp),
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(17.dp))
            .background(PremiumSurface.copy(alpha = 0.98f))
            .border(
                1.dp,
                PremiumBlueEdge.copy(alpha = if (pressed) 0.4f else 0.24f),
                RoundedCornerShape(17.dp)
            )
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClickLabel = "Open ${event.title}",
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .background(PremiumBlue)
        ) {
            if (event.imageUrl != null) {
                coil3.compose.AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(PremiumBlue, PremiumSurfaceRaised)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (kind) {
                            HomeContentKind.Event -> Icons.Outlined.Event
                            HomeContentKind.Movie -> Icons.Outlined.ConfirmationNumber
                            HomeContentKind.Sport -> Icons.Outlined.SportsSoccer
                        },
                        contentDescription = null,
                        tint = PremiumBlueEdge.copy(alpha = 0.55f),
                        modifier = Modifier.size(if (kind == HomeContentKind.Movie) 38.dp else 32.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, PremiumBackground.copy(alpha = 0.34f))
                        )
                    )
            )
            PremiumFavoriteButton(
                isFavorite = isFavorite,
                title = event.title,
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
            )
        }

        Column(modifier = Modifier.padding(11.dp)) {
            Text(
                text = event.title,
                color = PremiumWhite,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.date,
                color = PremiumSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = event.location,
                color = PremiumMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = event.price,
                color = PremiumOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PremiumFavoriteButton(
    isFavorite: Boolean,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = tween(90),
        label = "favoriteScale"
    )
    Box(
        modifier = modifier
            .size(38.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(
                if (isFavorite) PremiumOrange.copy(alpha = 0.2f)
                else PremiumBackground.copy(alpha = 0.76f)
            )
            .border(
                1.dp,
                if (isFavorite) PremiumOrange.copy(alpha = 0.42f)
                else Color.White.copy(alpha = 0.14f),
                CircleShape
            )
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClickLabel = if (isFavorite) "Remove $title from favorites" else "Add $title to favorites",
                onClick = onClick
            )
            .semantics {
                contentDescription = if (isFavorite) "Remove $title from favorites" else "Add $title to favorites"
                stateDescription = if (isFavorite) "Favorited" else "Not favorited"
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = {
                (fadeIn(tween(110)) + scaleIn(tween(130), initialScale = 0.7f))
                    .togetherWith(fadeOut(tween(80)) + scaleOut(tween(90), targetScale = 0.75f))
            },
            label = "favoriteIcon"
        ) { favorite ->
            Icon(
                imageVector = if (favorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (favorite) PremiumOrange else PremiumWhite,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PremiumBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val navItems = listOf(
        Triple("Home", Icons.Outlined.Home, Icons.Rounded.Home),
        Triple("Search", Icons.Outlined.Search, Icons.Rounded.Search),
        Triple("My Bookings", Icons.Outlined.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        Triple("Profile", Icons.Outlined.AccountCircle, Icons.Outlined.AccountCircle)
    )
    val selectedIndex = navItems.indexOfFirst { it.first == selectedItem }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF071A35).copy(alpha = 0.98f),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, PremiumBlueEdge.copy(alpha = 0.32f)),
            shadowElevation = 10.dp
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(5.dp)
            ) {
                val segmentWidth = maxWidth / navItems.size.toFloat()
                val indicatorOffset by animateDpAsState(
                    targetValue = segmentWidth * selectedIndex.toFloat(),
                    animationSpec = tween(240),
                    label = "bottomNavIndicator"
                )
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(PremiumOrange.copy(alpha = 0.13f))
                        .border(
                            1.dp,
                            PremiumOrange.copy(alpha = 0.2f),
                            RoundedCornerShape(17.dp)
                        )
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    navItems.forEach { item ->
                        val isSelected = item.first == selectedItem
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.08f else 0.96f,
                            animationSpec = tween(180),
                            label = "bottomNavIconScale"
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    role = Role.Tab,
                                    onClickLabel = item.first,
                                    onClick = { onItemSelected(item.first) }
                                )
                                .semantics { selected = isSelected },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.third else item.second,
                                contentDescription = item.first,
                                tint = if (isSelected) PremiumOrange else PremiumMuted,
                                modifier = Modifier
                                    .size(19.dp)
                                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.first,
                                color = if (isSelected) PremiumOrange else PremiumMuted,
                                fontSize = if (item.first == "My Bookings") 8.sp else 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumDrawer(
    onProfileClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 318.dp),
        drawerContainerColor = PremiumBackground,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0B3268), PremiumBackground, Color(0xFF041329))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.entrymyslotlogopcg),
                    contentDescription = "EntryMySlot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(178.dp).height(54.dp)
                )
                Text(
                    text = "Your booking companion",
                    color = PremiumSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
                Spacer(modifier = Modifier.height(22.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PremiumBlueEdge.copy(alpha = 0.24f))
                )
                Spacer(modifier = Modifier.height(18.dp))
                DrawerItem("Profile", Icons.Outlined.AccountCircle, onProfileClick)
                DrawerItem("My Bookings", Icons.Outlined.ConfirmationNumber, onBookingsClick)
                DrawerItem("Notifications", Icons.Outlined.Notifications, onNotificationsClick)
                DrawerItem("Settings", Icons.Outlined.Settings, onSettingsClick)
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(PremiumSurface.copy(alpha = 0.72f))
                        .border(
                            1.dp,
                            PremiumBlueEdge.copy(alpha = 0.22f),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PremiumOrange.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = PremiumOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(11.dp))
                    Column {
                        Text("EntryMySlot", color = PremiumWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Premium booking experience", color = PremiumMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (pressed) PremiumOrange.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PremiumBlue.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PremiumOrange, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = PremiumWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LocationSelectionScreen(
    selectedCity: String,
    onBackClick: () -> Unit,
    onCitySelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val locationFetcher = rememberLocationFetcher(onCityResolved = onCitySelected)
    val filteredDistricts = remember(searchQuery) {
        premiumDistricts.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    if (locationFetcher.showGpsDialog) {
        GpsDisabledDialog(
            onConfirm = locationFetcher.onOpenLocationSettings,
            onDismiss = locationFetcher.onDismissGpsDialog
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBackground)
    ) {
        GlowBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LocationTopBar(onBackClick = onBackClick)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Choose your city",
                    color = PremiumWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Find movies, events and venues near you.",
                    color = PremiumSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                CurrentLocationCard(
                    state = locationFetcher.state,
                    onClick = locationFetcher.onStart
                )
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                    placeholder = {
                        Text("Search city or district", color = PremiumMuted, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = PremiumSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        cursorColor = PremiumOrange,
                        focusedBorderColor = PremiumOrange,
                        unfocusedBorderColor = PremiumBlueEdge.copy(alpha = 0.34f),
                        focusedContainerColor = PremiumSurface,
                        unfocusedContainerColor = PremiumSurface
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "AVAILABLE CITIES",
                    color = PremiumMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)
            ) {
                items(items = filteredDistricts, key = { district -> district }) { district ->
                    CityRow(
                        city = district,
                        isSelected = district == selectedCity,
                        onClick = { onCitySelected(district) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PremiumIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            onClick = onBackClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Select Location",
            color = PremiumWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CurrentLocationCard(
    state: LocationFetchState,
    onClick: () -> Unit
) {
    val loading = state is LocationFetchState.Loading
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PremiumOrange,
            contentColor = PremiumWhite,
            disabledContainerColor = PremiumOrange.copy(alpha = 0.72f),
            disabledContentColor = PremiumWhite
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(19.dp),
                color = PremiumWhite,
                strokeWidth = 2.dp
            )
        } else {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state is LocationFetchState.Success) {
                    "Using ${state.cityName}"
                } else {
                    "Use my current location"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    if (state is LocationFetchState.PermissionDenied) {
        Text(
            text = "Location permission was not granted. Choose a city below.",
            color = PremiumSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 7.dp)
        )
    } else if (state is LocationFetchState.Error) {
        Text(
            text = state.message,
            color = PremiumSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 7.dp)
        )
    }
}

@Composable
private fun CityRow(city: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (isSelected) PremiumOrange.copy(alpha = 0.11f)
                else PremiumSurface.copy(alpha = 0.82f)
            )
            .border(
                1.dp,
                if (isSelected) PremiumOrange.copy(alpha = 0.28f)
                else PremiumBlueEdge.copy(alpha = 0.2f),
                RoundedCornerShape(15.dp)
            )
            .clickable(
                role = Role.RadioButton,
                onClickLabel = "Select $city",
                onClick = onClick
            )
            .semantics { selected = isSelected }
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = if (isSelected) PremiumOrange else PremiumMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = city,
            color = PremiumWhite,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = premiumShortCityName(city),
            color = if (isSelected) PremiumOrange else PremiumMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private val premiumDistricts = listOf(
    "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore",
    "Dharmapuri", "Dindigul", "Erode", "Kallakurichi", "Kancheepuram",
    "Kanniyakumari", "Karur", "Krishnagiri", "Madurai", "Mayiladuthurai",
    "Nagapattinam", "Namakkal", "Nilgiris", "Perambalur", "Pudukkottai",
    "Ramanathapuram", "Ranipet", "Salem", "Sivaganga", "Tenkasi",
    "Thanjavur", "Theni", "Thoothukudi", "Tiruchirappalli", "Tirunelveli",
    "Tirupathur", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur",
    "Vellore", "Viluppuram", "Virudhunagar"
)

private fun premiumShortCityName(city: String): String = when (city) {
    "Chennai" -> "CHN"
    "Coimbatore" -> "CBE"
    "Madurai" -> "MDU"
    "Tiruchirappalli", "Trichy" -> "TRY"
    "Salem" -> "SLM"
    "Erode" -> "ERD"
    "Vellore" -> "VEL"
    "Tirunelveli" -> "TNV"
    "Thoothukudi" -> "TUT"
    else -> if (city.length > 3) city.take(3).uppercase() else city.uppercase()
}
