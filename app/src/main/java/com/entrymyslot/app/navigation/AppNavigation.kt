package com.entrymyslot.app.navigation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.entrymyslotbe.app.auth.AuthScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.entrymyslot.app.core.components.EntryBottomNavigation
import com.entrymyslot.app.core.components.PremiumEmptyState
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.booking.PendingCheckout
import com.entrymyslot.app.data.booking.PendingMovieCheckout
import com.entrymyslot.app.data.booking.PendingEventCheckout
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.model.BookingDetails
import com.entrymyslot.app.data.model.BookingType
import com.entrymyslot.app.screens.auth.AuthScreen
import com.entrymyslot.app.screens.home.HomeScreen
import com.entrymyslot.app.screens.home.LocationSelectionScreen
import com.entrymyslot.app.screens.movies.CinemaSelectionScreen
import com.entrymyslot.app.screens.movies.MovieBookingScreen
import com.entrymyslot.app.screens.movies.MovieDetailsScreen
import com.entrymyslot.app.screens.payment.PaymentScreen
import com.entrymyslot.app.screens.turf.TurfScreen
import com.entrymyslot.app.screens.turf.TurfBookingScreen
import com.entrymyslot.app.screens.events.EventDetailsScreen
import com.entrymyslot.app.screens.events.EventBookingScreen
import com.entrymyslot.app.screens.profile.TermsPolicyScreen
import com.entrymyslot.app.screens.profile.ProfileScreen
import com.entrymyslot.app.screens.booking.BookingScreen
import com.entrymyslot.app.screens.search.SearchResultType
import com.entrymyslot.app.screens.search.SearchScreen
import com.entrymyslot.app.screens.ticket.TicketScreen
import com.entrymyslot.app.screens.onboarding.PermissionsScreen
import com.entrymyslot.app.screens.onboarding.SplashScreen
import com.entrymyslot.app.screens.manager.ManagerDashboardScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as EntryMySlotApp
    val locationPreferences = remember {
        context.getSharedPreferences("entry_my_slot_preferences", android.content.Context.MODE_PRIVATE)
    }

    var selectedCity by remember {
        mutableStateOf(locationPreferences.getString("selected_city", "").orEmpty())
    }
    val session by app.appContainer.backend.sessions.state.collectAsStateWithLifecycle()
    val isLoggedIn = AuthScope.USER in session.authenticatedScopes
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    LaunchedEffect(isLoggedIn, currentRoute) {
        if (!isLoggedIn && currentRoute != null && currentRoute !in setOf("auth", "splash")) {
            app.appContainer.pendingCheckoutStore.clear()
            navController.navigate("auth") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
        }
    }
    val navbarRoutes = setOf("home", "search/{type}", "bookings", "profile")
    
    var showForcedLocationDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = currentRoute != null && currentRoute !in setOf("splash", "home", "auth", "permissions", "ticket/{type}/{itemId}/{bookingKey}/{ticketUuid}")) {
        if (currentRoute == "location_selection" && selectedCity.isBlank()) {
            showForcedLocationDialog = true
        } else {
            navController.popBackStack()
        }
    }
    
    var isHomeDrawerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != "home") isHomeDrawerOpen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "splash",
            enterTransition = { slideInHorizontally(tween(150)) { it / 12 } },
            exitTransition = { slideOutHorizontally(tween(120)) { -it / 14 } },
            popEnterTransition = { slideInHorizontally(tween(150)) { -it / 12 } },
            popExitTransition = { slideOutHorizontally(tween(120)) { it / 14 } }
        ) {

        composable("terms_policy") {
            TermsPolicyScreen(onBackClick = { navController.popBackStack() })
        }

        composable("splash") {
            SplashScreen(
                onFinished = {
                    val destination = when {
                        !isLoggedIn -> "auth"
                        selectedCity.isNotBlank() -> "home"
                        else -> "location_selection"
                    }
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    val destination = if (selectedCity.isNotBlank()) {
                        "home"
                    } else {
                        "location_selection"
                    }
                    navController.navigate(destination) {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("permissions") {
            PermissionsScreen(
                onPermissionsComplete = { detectedCity ->
                    if (!detectedCity.isNullOrBlank()) {
                        selectedCity = detectedCity
                        locationPreferences.edit()
                            .putString("selected_city", detectedCity)
                            .putBoolean("onboarding_completed", true)
                            .apply()
                        navController.navigate("home") {
                            popUpTo("permissions") { inclusive = true }
                        }
                    } else {
                        selectedCity = ""
                        navController.navigate("location_selection") {
                            popUpTo("permissions") { inclusive = true }
                        }
                    }
                },
                onDeclineLocation = {
                    selectedCity = ""
                    navController.navigate("location_selection") {
                        popUpTo("permissions") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onTermsClick = { navController.navigate("terms_policy") },
                onEventClick = { event ->
                    navController.navigate("event_details/${event.id}")
                },
                onSportClick = { sport ->
                    navController.navigate("turf_details/${sport.id}")
                },
                onMovieBookClick = {
                    navController.navigate("movie_details/${it.id}")
                },
                onSearchClick = { navController.navigate("search/all") },
                onLocationClick = { navController.navigate("location_selection") },
                onDrawerVisibilityChange = { isHomeDrawerOpen = it },
                onPartnerClick = { navController.navigate("manager_dashboard") },
                selectedCity = selectedCity,
                onCategoryClick = { category ->
                    when (category) {
                        "Movies", "Latest Movies", "Popular Movies" -> navController.navigate("search/movie")
                        "Sports", "Sports Near You", "Popular Turf" -> navController.navigate("search/sport")
                        "Events", "Popular Events" -> navController.navigate("search/event")
                    }
                },
                onBottomNavigationClick = { item ->
                    navController.navigateToTopLevel(item)
                }
            )
        }

        composable("location_selection") {
            LocationSelectionScreen(
                selectedCity = selectedCity,
                onBackClick = { 
                    if (selectedCity.isBlank()) {
                        showForcedLocationDialog = true
                    } else {
                        navController.popBackStack() 
                    }
                },
                onCitySelected = { city ->
                    selectedCity = city
                    locationPreferences.edit()
                        .putString("selected_city", city)
                        .putBoolean("onboarding_completed", true)
                        .apply()
                    if (navController.previousBackStackEntry == null) {
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable("search/{type}") { backStackEntry ->
            val initialType = when (backStackEntry.arguments?.getString("type")) {
                "movie" -> SearchResultType.MOVIE
                "sport" -> SearchResultType.SPORT
                "event" -> SearchResultType.EVENT
                else -> null
            }
            SearchScreen(
                selectedCity = selectedCity,
                initialType = initialType,
                onBackClick = { navController.popBackStack() },
                onResultClick = { result ->
                    when (result.type) {
                        SearchResultType.MOVIE -> navController.navigate("movie_details/${result.item.id}")
                        SearchResultType.SPORT -> navController.navigate("turf_details/${result.item.id}")
                        SearchResultType.EVENT -> navController.navigate("event_details/${result.item.id}")
                    }
                },
                onBottomNavigationClick = { item ->
                    navController.navigateToTopLevel(item)
                }
            )
        }

        composable("movie_details/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            MovieDetailsScreen(
                movieId = movieId,
                onBackClick = { navController.popBackStack() },
                onBookClick = { navController.navigate("cinema_selection/$movieId") { launchSingleTop = true } }
            )
        }

        composable("event_details/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()

            EventDetailsScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onBookTicketsClick = {
                    navController.navigate("event_booking/$eventId") { launchSingleTop = true }
                }
            )
        }

        composable("event_booking/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()

            EventBookingScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = {
                    navController.navigate("payment/EVENT/$eventId") { launchSingleTop = true }
                }
            )
        }

        composable("bookings") {
            BookingScreen(
                onBackClick = { navController.popBackStack() },
                onBottomNavigationClick = { item ->
                    navController.navigateToTopLevel(item)
                },
                onViewTicketClick = { booking ->
                    val bookingKey = when (booking.type) {
                        BookingType.EVENT, BookingType.TURF -> booking.id.substringAfter(':')
                        BookingType.MOVIE -> booking.bookingReference
                    }
                    val ticketRoute = listOf(
                        booking.type.name,
                        booking.itemId,
                        bookingKey,
                        "_"
                    ).joinToString("/") { Uri.encode(it) }
                    navController.navigate("ticket/$ticketRoute") { launchSingleTop = true }
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onBookingClick = {
                    navController.navigateToTopLevel("My Bookings")
                },
                onPartnerClick = {
                    navController.navigate("manager_dashboard")
                },
                onLogoutClick = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("manager_dashboard") {
            ManagerDashboardScreen(onBackClick = { navController.popBackStack() })
        }

        composable("turf_details/{sportId}") { backStackEntry ->
            val sportId = backStackEntry.arguments?.getString("sportId").orEmpty()
            TurfScreen(
                sportId = sportId,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = {
                    navController.navigate("turf_booking/$sportId") { launchSingleTop = true }
                }
            )
        }

        composable("turf_booking/{sportId}") { backStackEntry ->
            val sportId = backStackEntry.arguments?.getString("sportId").orEmpty()
            TurfBookingScreen(
                turfId = sportId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = {
                    navController.navigate("payment/TURF/$sportId") { launchSingleTop = true }
                }
            )
        }

        composable("cinema_selection/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            CinemaSelectionScreen(
                movieId = movieId,
                onBackClick = { navController.popBackStack() },
                onTimeSelected = { showtimeId ->
                    navController.navigate("movie_booking/$movieId/$showtimeId") { launchSingleTop = true }
                }
            )
        }

        composable("movie_booking/{movieId}/{showtimeId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            val showtimeId = backStackEntry.arguments?.getString("showtimeId")?.toIntOrNull() ?: -1

            MovieBookingScreen(
                movieId = movieId,
                showtimeId = showtimeId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = {
                    navController.navigate("payment/MOVIE/$movieId") { launchSingleTop = true }
                }
            )
        }

        composable("payment/{type}/{itemId}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "MOVIE"
            val itemId = backStackEntry.arguments?.getString("itemId").orEmpty()
            val checkout by app.appContainer.pendingCheckoutStore.current.collectAsStateWithLifecycle()
            val details = checkout?.toBookingDetails()?.takeIf {
                it.itemId == itemId && it.category.name == type
            }
            if (details == null) {
                PremiumEmptyState(
                    modifier = Modifier.fillMaxSize(),
                    title = "Checkout unavailable",
                    message = "Go back to review your selection. If you already paid, check My Bookings for your ticket.",
                    actionText = "Back",
                    onAction = {
                        if (!navController.popBackStack()) navController.navigate("home")
                    }
                )
                return@composable
            }

            PaymentScreen(
                bookingDetails = details,
                onBackClick = { navController.popBackStack() },
                onPaySuccess = { confirmed ->
                    val ticketRoute = listOf(
                        confirmed.type,
                        confirmed.itemId,
                        confirmed.bookingKey,
                        confirmed.ticketUuid
                    ).joinToString("/") { Uri.encode(it) }
                    navController.navigate("ticket/$ticketRoute") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("ticket/{type}/{itemId}/{bookingKey}/{ticketUuid}") { backStackEntry ->
            val closeTicket = {
                app.appContainer.pendingCheckoutStore.clear()
                if (navController.previousBackStackEntry?.destination?.route == "bookings") {
                    navController.popBackStack()
                } else {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
                Unit
            }
            BackHandler(onBack = closeTicket)
            TicketScreen(
                type = backStackEntry.arguments?.getString("type").orEmpty(),
                itemId = backStackEntry.arguments?.getString("itemId").orEmpty(),
                bookingKey = backStackEntry.arguments?.getString("bookingKey").orEmpty(),
                ticketUuid = backStackEntry.arguments?.getString("ticketUuid").orEmpty(),
                onBackClick = closeTicket,
                onDoneClick = closeTicket
            )
        }
        }

        if (currentRoute in navbarRoutes && !isHomeDrawerOpen && selectedCity.isNotBlank()) {
            val selectedItem = when (currentRoute) {
                "home" -> "Home"
                "search/{type}" -> "Search"
                "bookings" -> "My Bookings"
                "profile" -> "Profile"
                else -> ""
            }
            EntryBottomNavigation(
                selectedItem = selectedItem,
                onItemSelected = { item ->
                    navController.navigateToTopLevel(item)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showForcedLocationDialog) {
            AlertDialog(
                onDismissRequest = { /* Don't allow dismiss */ },
                title = { Text("Pick a city to continue", fontWeight = FontWeight.Bold) },
                text = { Text("Before you go back, choose the city where you want to discover movies, sports, and events.") },
                confirmButton = {
                    Button(
                        onClick = { showForcedLocationDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA580B))
                    ) {
                        Text("Select City")
                    }
                },
                containerColor = Color(0xFF0B274F),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFA8B8CF)
            )
        }
    }
}

private fun NavHostController.navigateToTopLevel(item: String) {
    val destination = when (item) {
        "Home" -> "home"
        "Search" -> "search/all"
        "My Bookings" -> "bookings"
        "Profile" -> "profile"
        else -> return
    }
    val route = if (item == "Search") "search/{type}" else destination
    if (currentDestination?.route == route) return
    navigate(destination) {
        // Splash has already been removed; Home is the persistent tab root.
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun PendingCheckout.toBookingDetails(): BookingDetails = when (this) {
    is PendingMovieCheckout -> {
        val showTime = runCatching {
            java.time.OffsetDateTime.parse(showDatetime).atZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata"))
        }.getOrNull()
        BookingDetails(
        itemId = itemId, title = movieTitle, category = BookingType.MOVIE,
        date = showTime?.toLocalDate()?.toString() ?: showDatetime.substringBefore('T'),
        time = showTime?.toLocalTime()?.toString()?.take(5) ?: showDatetime.substringAfter('T').take(5),
        location = cinemaName, details = seatLabels.joinToString(", "),
        baseAmount = java.math.BigDecimal(bill.subtotalPaise).movePointLeft(2), convenienceFee = bill.platformFeePaise?.let { java.math.BigDecimal(it).movePointLeft(2) },
        taxes = bill.gstTotalPaise?.let { java.math.BigDecimal(it).movePointLeft(2) }
        )
    }
    is PendingEventCheckout -> BookingDetails(
        itemId = itemId, title = title, category = BookingType.EVENT,
        date = eventDate, time = eventTime, location = venueLocation,
        details = "${bill.quantity} ticket${if (bill.quantity == 1) "" else "s"}",
        baseAmount = java.math.BigDecimal(bill.subtotalPaise).movePointLeft(2), convenienceFee = bill.platformFeePaise?.let { java.math.BigDecimal(it).movePointLeft(2) },
        taxes = bill.gstTotalPaise?.let { java.math.BigDecimal(it).movePointLeft(2) }
    )
    is PendingTurfCheckout -> BookingDetails(
        itemId = itemId, title = resourceName, category = BookingType.TURF,
        date = bookingDate, time = formattedTime, location = venueLocation,
        details = formattedTime, baseAmount = java.math.BigDecimal(bill.subtotalPaise).movePointLeft(2),
        convenienceFee = bill.platformFeePaise?.let { java.math.BigDecimal(it).movePointLeft(2) }, taxes = bill.gstTotalPaise?.let { java.math.BigDecimal(it).movePointLeft(2) }
    )
}
