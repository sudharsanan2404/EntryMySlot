package com.entrymyslot.app.navigation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.entrymyslot.app.core.components.EntryBottomNavigation
import com.entrymyslot.app.AppAuthState
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.booking.PendingCheckout
import com.entrymyslot.app.data.booking.PendingMovieCheckout
import com.entrymyslot.app.data.booking.PendingEventCheckout
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.FakeData
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
import com.entrymyslot.app.screens.profile.ProfileScreen
import com.entrymyslot.app.screens.test.TestScreen
import com.entrymyslot.app.screens.booking.BookingScreen
import com.entrymyslot.app.screens.search.SearchResultType
import com.entrymyslot.app.screens.search.SearchScreen
import com.entrymyslot.app.screens.ticket.TicketScreen
import com.entrymyslot.app.screens.wishlist.WishlistScreen

@Composable
fun AppNavigation(
    authState: AppAuthState,
    onAuthenticated: () -> Unit,
    onLoggedOut: () -> Unit
) {

    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as EntryMySlotApp

    if (authState == AppAuthState.Loading) {
        return
    }

    var selectedCity by remember { mutableStateOf(FakeData.currentUser.city) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val navbarRoutes = setOf("home", "search/{type}", "bookings", "profile", "wishlist")
    BackHandler(enabled = currentRoute != null && currentRoute !in setOf("home", "auth", "ticket/{type}/{itemId}/{bookingKey}/{ticketUuid}")) {
        navController.popBackStack()
    }
    var isHomeDrawerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != "home") isHomeDrawerOpen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = if (authState == AppAuthState.Unauthenticated) "auth" else "home",
            enterTransition = { slideInHorizontally(tween(150)) { it / 12 } },
            exitTransition = { slideOutHorizontally(tween(120)) { -it / 14 } },
            popEnterTransition = { slideInHorizontally(tween(150)) { -it / 12 } },
            popExitTransition = { slideOutHorizontally(tween(120)) { it / 14 } }
        ) {

        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    onAuthenticated()
                    navController.navigate("home") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
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
                onWishlistClick = { navController.navigate("wishlist") },
                onLocationClick = { navController.navigate("location_selection") },
                onDrawerVisibilityChange = { isHomeDrawerOpen = it },
                selectedCity = selectedCity,
                onCategoryClick = { category ->
                    when (category) {
                        "Movies", "Latest Movies", "Popular Movies" -> navController.navigate("search/movie")
                        "Sports", "Sports Near You", "Popular Turf" -> navController.navigate("search/sport")
                        "Events", "Popular Events" -> navController.navigate("search/event")
                    }
                },
                onBottomNavigationClick = { item ->
                    when (item) {
                        "My Bookings" -> navController.navigate("bookings")
                        "Profile" -> navController.navigate("profile")
                        "Search" -> navController.navigate("search/all")
                    }
                }
            )
        }

        composable("location_selection") {
            LocationSelectionScreen(
                selectedCity = selectedCity,
                onBackClick = { navController.popBackStack() },
                onCitySelected = { city ->
                    selectedCity = city
                    navController.popBackStack()
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
                    when (item) {
                        "Home" -> navController.navigate("home")
                        "My Bookings" -> navController.navigate("bookings")
                        "Profile" -> navController.navigate("profile")
                    }
                }
            )
        }

        composable("movie_details/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            MovieDetailsScreen(
                movieId = movieId,
                onBackClick = { navController.popBackStack() },
                onBookClick = { navController.navigate("cinema_selection/$movieId") }
            )
        }

        composable("event_details/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()

            EventDetailsScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onBookTicketsClick = {
                    navController.navigate("event_booking/$eventId")
                }
            )
        }

        composable("event_booking/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()

            EventBookingScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = {
                    navController.navigate("payment/EVENT/$eventId")
                }
            )
        }

        composable("bookings") {
            BookingScreen(
                onBackClick = { navController.popBackStack() },
                onBottomNavigationClick = { item ->
                    when (item) {
                        "Home" -> navController.navigate("home")
                        "Profile" -> navController.navigate("profile")
                        "Search" -> navController.navigate("search/all")
                    }
                },
                onViewTicketClick = { booking ->
                    val bookingKey = when (booking.type) {
                        BookingType.EVENT -> booking.id.substringAfter(':')
                        BookingType.MOVIE, BookingType.TURF -> booking.bookingReference
                    }
                    val ticketRoute = listOf(
                        booking.type.name,
                        booking.itemId,
                        bookingKey,
                        "_"
                    ).joinToString("/") { Uri.encode(it) }
                    navController.navigate("ticket/$ticketRoute")
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onBottomNavigationClick = { item ->
                    when (item) {
                        "Home" -> navController.navigate("home")
                        "My Bookings" -> navController.navigate("bookings")
                        "Search" -> navController.navigate("search/all")
                    }
                },
                onBookingClick = {
                    navController.navigate("bookings")
                },
                onUsernameClick = {
                    navController.navigate("test")
                },
                onLogoutClick = {
                    onLoggedOut()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("test") {
            TestScreen(onBackClick = { navController.popBackStack() })
        }

        composable("wishlist") {
            WishlistScreen(
                onBackClick = { navController.popBackStack() },
                onItemClick = { item, category ->
                    when (category) {
                        "MOVIE" -> navController.navigate("movie_details/${item.id}")
                        "SPORT" -> navController.navigate("turf_details/${item.id}")
                        else -> navController.navigate("event_details/${item.id}")
                    }
                },
                onBottomNavigationClick = { item ->
                    when (item) {
                        "Home" -> navController.navigate("home")
                        "Search" -> navController.navigate("search/all")
                        "My Bookings" -> navController.navigate("bookings")
                        "Profile" -> navController.navigate("profile")
                    }
                }
            )
        }

        composable("turf_details/{sportId}") { backStackEntry ->
            val sportId = backStackEntry.arguments?.getString("sportId").orEmpty()
            TurfScreen(
                sportId = sportId,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = {
                    navController.navigate("turf_booking/$sportId")
                }
            )
        }

        composable("turf_booking/{sportId}") { backStackEntry ->
            val sportId = backStackEntry.arguments?.getString("sportId").orEmpty()
            TurfBookingScreen(
                turfId = sportId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = {
                    navController.navigate("payment/TURF/$sportId")
                }
            )
        }

        composable("cinema_selection/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            CinemaSelectionScreen(
                movieId = movieId,
                onBackClick = { navController.popBackStack() },
                onTimeSelected = { showtimeId ->
                    navController.navigate("movie_booking/$movieId/$showtimeId")
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
                    navController.navigate("payment/MOVIE/$movieId")
                }
            )
        }

        composable("payment/{type}/{itemId}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "MOVIE"
            val itemId = backStackEntry.arguments?.getString("itemId").orEmpty()
            val bookingType = runCatching { BookingType.valueOf(type) }.getOrDefault(BookingType.MOVIE)
            val details = app.appContainer.pendingCheckoutStore.current.value?.toBookingDetails()
                ?: return@composable

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
            TicketScreen(
                type = backStackEntry.arguments?.getString("type").orEmpty(),
                itemId = backStackEntry.arguments?.getString("itemId").orEmpty(),
                bookingKey = backStackEntry.arguments?.getString("bookingKey").orEmpty(),
                ticketUuid = backStackEntry.arguments?.getString("ticketUuid").orEmpty(),
                onBackClick = {
                    app.appContainer.pendingCheckoutStore.clear()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onDoneClick = {
                    app.appContainer.pendingCheckoutStore.clear()
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                }
            )
        }
        }

        if (currentRoute in navbarRoutes && !isHomeDrawerOpen) {
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
                    val destination = when (item) {
                        "Home" -> "home"
                        "Search" -> "search/all"
                        "My Bookings" -> "bookings"
                        else -> "profile"
                    }
                    if (selectedItem != item) {
                        navController.navigate(destination) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("home") { saveState = true }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (authState == AppAuthState.AuthenticatedOffline && currentRoute != "auth") {
            Text(
                text = "No internet. Signed-in session is kept; online data may be unavailable.",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF9A3412))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

private fun PendingCheckout.toBookingDetails(): BookingDetails = when (this) {
    is PendingMovieCheckout -> BookingDetails(
        itemId = itemId, title = movieTitle, category = BookingType.MOVIE,
        date = showDatetime.substringBefore('T'), time = showDatetime.substringAfter('T').take(5),
        location = cinemaName, details = seatLabels.joinToString(", "),
        baseAmount = bill.subtotalPaise / 100, convenienceFee = bill.platformFeePaise / 100,
        taxes = bill.gstTotalPaise / 100
    )
    is PendingEventCheckout -> BookingDetails(
        itemId = itemId, title = title, category = BookingType.EVENT,
        date = "", time = "", location = zoneName,
        details = "${bill.quantity} ticket${if (bill.quantity == 1) "" else "s"}",
        baseAmount = bill.subtotalPaise / 100, convenienceFee = bill.platformFeePaise / 100,
        taxes = bill.gstTotalPaise / 100
    )
    is PendingTurfCheckout -> BookingDetails(
        itemId = itemId, title = resourceName, category = BookingType.TURF,
        date = startsAt.substringBefore('T'), time = formattedTime, location = resourceName,
        details = formattedTime, baseAmount = bill.subtotalPaise / 100,
        convenienceFee = bill.platformFeePaise / 100, taxes = bill.gstTotalPaise / 100
    )
}
