package com.entrymyslot.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.entrymyslot.app.core.components.EntryBottomNavigation
import com.entrymyslot.app.core.storage.AuthTokenStore
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
import com.entrymyslot.app.screens.booking.BookingScreen
import com.entrymyslot.app.screens.search.SearchResultType
import com.entrymyslot.app.screens.search.SearchScreen
import com.entrymyslot.app.screens.ticket.TicketScreen
import com.entrymyslot.app.screens.wishlist.WishlistScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppNavigation(
    authTokenStore: AuthTokenStore
) {

    val navController = rememberNavController()
    val accessToken by authTokenStore.accessToken.collectAsStateWithLifecycle(initialValue = "LOADING")

    if (accessToken == "LOADING") {
        // You could show a splash screen here
        return
    }

    var selectedCity by remember { mutableStateOf(FakeData.currentUser.city) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val navbarRoutes = setOf("home", "search/{type}", "bookings", "profile", "wishlist")
    BackHandler(enabled = currentRoute != null && currentRoute !in setOf("home", "auth", "ticket/{bookingId}")) {
        navController.popBackStack()
    }
    var isHomeDrawerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != "home") isHomeDrawerOpen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = if (accessToken != null) "home" else "auth",
            enterTransition = { slideInHorizontally(tween(150)) { it / 12 } },
            exitTransition = { slideOutHorizontally(tween(120)) { -it / 14 } },
            popEnterTransition = { slideInHorizontally(tween(150)) { -it / 12 } },
            popExitTransition = { slideOutHorizontally(tween(120)) { it / 14 } }
        ) {

        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
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
                movies = FakeData.movies,
                sports = FakeData.turfs,
                events = FakeData.events,
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
            val movieId = backStackEntry.arguments?.getString("movieId")
            val movie = movieId?.let(FakeData::getMovieById) ?: FakeData.movies.first()
            MovieDetailsScreen(
                movie = movie,
                onBackClick = { navController.popBackStack() },
                onBookClick = { navController.navigate("cinema_selection/${movie.id}") }
            )
        }

        composable("event_details/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val event = eventId?.let(FakeData::getEventById) ?: FakeData.events.first()

            EventDetailsScreen(
                event = event,
                onBackClick = { navController.popBackStack() },
                onBookTicketsClick = {
                    navController.navigate("event_booking/${event.id}")
                }
            )
        }

        composable("event_booking/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val event = eventId?.let(FakeData::getEventById) ?: FakeData.events.first()

            EventBookingScreen(
                event = event,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { _ ->
                    navController.navigate("payment/EVENT/${event.id}")
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
                    navController.navigate("ticket/${booking.id}")
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
                onLogoutClick = {
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
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
            val sportId = backStackEntry.arguments?.getString("sportId") ?: FakeData.turfs.first().id
            TurfScreen(
                sportId = sportId,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = {
                    navController.navigate("turf_booking/$sportId")
                }
            )
        }

        composable("turf_booking/{sportId}") { backStackEntry ->
            val sportId = backStackEntry.arguments?.getString("sportId") ?: FakeData.turfs.first().id
            TurfBookingScreen(
                turfId = sportId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = {
                    navController.navigate("payment/TURF/$sportId")
                }
            )
        }

        composable("cinema_selection/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: FakeData.movies.first().id
            CinemaSelectionScreen(
                movieId = movieId,
                onBackClick = { navController.popBackStack() },
                onTimeSelected = { cinema, time, _ ->
                    navController.navigate("movie_booking/$movieId/${cinema.id}/$time")
                }
            )
        }

        composable("movie_booking/{movieId}/{cinemaId}/{time}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: FakeData.movies.first().id
            val cinemaId = backStackEntry.arguments?.getString("cinemaId")
            val time = backStackEntry.arguments?.getString("time") ?: ""
            val cinema = cinemaId?.let(FakeData::getCinemaById) ?: FakeData.cinemas.first()

            MovieBookingScreen(
                movieId = movieId,
                cinema = cinema,
                initialTime = time,
                selectedDate = java.util.Calendar.getInstance(), 
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
            val details = FakeData.createBookingDetails(itemId, bookingType)

            PaymentScreen(
                bookingDetails = details,
                onBackClick = { navController.popBackStack() },
                onPaySuccess = {
                    navController.navigate("ticket/confirmed") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("ticket/{bookingId}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: "confirmed"
            val booking = FakeData.bookings.find { it.id == bookingId }
            val ticket = booking?.let(FakeData::getTicket) ?: FakeData.confirmedTicket
            TicketScreen(
                ticket = ticket,
                onBackClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onDoneClick = {
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
    }
}
