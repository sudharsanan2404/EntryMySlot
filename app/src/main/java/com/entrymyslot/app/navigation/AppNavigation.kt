package com.entrymyslot.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.core.storage.AuthTokenStore
import com.entrymyslot.app.screens.auth.AuthScreen
import com.entrymyslot.app.screens.booking.BookingScreen
import com.entrymyslot.app.screens.debug.TestScreen
import com.entrymyslot.app.screens.events.EventBookingScreen
import com.entrymyslot.app.screens.events.EventViewModel
import com.entrymyslot.app.screens.events.EventsScreen
import com.entrymyslot.app.screens.home.HomeScreen
import com.entrymyslot.app.screens.movies.CinemaSelectionScreen
import com.entrymyslot.app.screens.movies.MovieBookingScreen
import com.entrymyslot.app.screens.movies.MovieViewModel
import com.entrymyslot.app.screens.movies.MoviesScreen
import com.entrymyslot.app.screens.profile.ProfileScreen
import com.entrymyslot.app.screens.turf.TurfBookingScreen
import com.entrymyslot.app.screens.turf.TurfDetailScreen
import com.entrymyslot.app.screens.turf.TurfScreen
import com.entrymyslot.app.screens.turf.TurfViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Events : Screen("events")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun route(eventId: Any) = "event_detail/$eventId"
    }
    object EventBooking : Screen("event_booking/{eventId}") {
        fun route(eventId: Any) = "event_booking/$eventId"
    }
    object Movies : Screen("movies")
    object MovieDetail : Screen("movie_detail/{movieId}") {
        fun route(movieId: Any) = "movie_detail/$movieId"
    }
    object CinemaSelection : Screen("cinema_selection/{movieId}") {
        fun route(movieId: Any) = "cinema_selection/$movieId"
    }
    object MovieBooking : Screen("movie_booking/{showtimeId}") {
        fun route(showtimeId: Any) = "movie_booking/$showtimeId"
    }
    object Turf : Screen("turf")
    object TurfDetail : Screen("turf_detail/{venueId}") {
        fun route(venueId: Any) = "turf_detail/$venueId"
    }
    object TurfBooking : Screen("turf_booking/{resourceId}") {
        fun route(resourceId: String) = "turf_booking/$resourceId"
    }
    object MyBookings : Screen("my_bookings")
    object Profile : Screen("profile")
    object Category : Screen("category/{categoryName}") {
        fun route(categoryName: String) = "category/$categoryName"
    }
    object Booking : Screen("booking")
    object Ticket : Screen("ticket/{bookingId}") {
        fun route(bookingId: Any) = "ticket/$bookingId"
    }
    object ListYourVenue : Screen("list_your_venue")
    object Debug : Screen("debug")
}

@Composable
fun AppNavigation(
    authTokenStore: AuthTokenStore
) {

    val navController = rememberNavController()
    val accessToken by authTokenStore.accessToken.collectAsState(initial = "LOADING")

    if (accessToken == "LOADING") {
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (accessToken != null) Screen.Home.route else Screen.Auth.route
    ) {

        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onCategoryClick = { category ->
                    when {
                        category.contains("Movie", ignoreCase = true) -> navController.navigate(Screen.Movies.route)
                        category.contains("Sport", ignoreCase = true) || category.contains("Turf", ignoreCase = true) -> navController.navigate(Screen.Turf.route)
                        category.contains("Event", ignoreCase = true) || category.contains("Concert", ignoreCase = true) -> navController.navigate(Screen.Events.route)
                        else -> navController.navigate(Screen.Category.route(category))
                    }
                },
                onEventClick = { event ->
                    navController.navigate(Screen.EventDetail.route(event.id))
                },
                onBottomNavigationClick = { item ->
                    when (item) {
                        "Home" -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        "My Bookings" -> navController.navigate(Screen.MyBookings.route)
                        "Wallet" -> navController.navigate(Screen.Booking.route)
                        "Profile" -> navController.navigate(Screen.Profile.route)
                    }
                },
                onSportClick = { event ->
                    navController.navigate(Screen.Turf.route)
                },
                onMovieBookClick = { movie ->
                    navController.navigate(Screen.Movies.route)
                }
            )
        }

        composable(Screen.Events.route) {
            val eventViewModel: EventViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EventViewModel(EntryMySlotApp.instance.appContainer.eventRepository) as T
                    }
                }
            )
            val uiState by eventViewModel.uiState.collectAsState()

            EventsScreen(
                events = uiState.events,
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.route(eventId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val eventViewModel: EventViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EventViewModel(EntryMySlotApp.instance.appContainer.eventRepository) as T
                    }
                }
            )
            val uiState by eventViewModel.uiState.collectAsState()

            EventsScreen(
                events = uiState.events,
                selectedEventId = eventId,
                onEventClick = { id ->
                    navController.navigate(Screen.EventDetail.route(id))
                },
                onBookClick = { id ->
                    navController.navigate(Screen.EventBooking.route(id))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EventBooking.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventIdStr = backStackEntry.arguments?.getString("eventId") ?: "0"
            val eventId = eventIdStr.toLongOrNull() ?: 0L
            EventBookingScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onBookingSuccess = { navController.navigate(Screen.MyBookings.route) }
            )
        }

        composable(Screen.Movies.route) {
            val movieViewModel: MovieViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MovieViewModel(EntryMySlotApp.instance.appContainer.movieRepository) as T
                    }
                }
            )
            val uiState by movieViewModel.uiState.collectAsState()
            
            MoviesScreen(
                movies = uiState.movies,
                onMovieClick = { movieId ->
                    navController.navigate(Screen.CinemaSelection.route(movieId))
                },
                onCinemaSelect = { movieId ->
                    navController.navigate(Screen.CinemaSelection.route(movieId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CinemaSelection.route,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            CinemaSelectionScreen(
                movieId = movieId,
                onCinemaClick = { cinemaId ->
                    navController.navigate(Screen.MovieBooking.route(cinemaId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MovieBooking.route,
            arguments = listOf(navArgument("showtimeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val showtimeId = backStackEntry.arguments?.getString("showtimeId") ?: ""
            MovieBookingScreen(
                showtimeId = showtimeId,
                onBackClick = { navController.popBackStack() },
                onBookingSuccess = { navController.navigate(Screen.MyBookings.route) }
            )
        }

        composable(Screen.Turf.route) {
            val turfViewModel: TurfViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return TurfViewModel(EntryMySlotApp.instance.appContainer.turfRepository) as T
                    }
                }
            )
            val uiState by turfViewModel.uiState.collectAsState()

            TurfScreen(
                venues = uiState.venues,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onBackClick = { navController.popBackStack() },
                onVenueClick = { venueId ->
                    navController.navigate(Screen.TurfDetail.route(venueId))
                }
            )
        }

        composable(
            route = Screen.TurfDetail.route,
            arguments = listOf(navArgument("venueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val venueId = backStackEntry.arguments?.getString("venueId") ?: ""
            TurfDetailScreen(
                venueId = venueId,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = { resourceId ->
                    navController.navigate(Screen.TurfBooking.route(resourceId))
                }
            )
        }

        composable(
            route = Screen.TurfBooking.route,
            arguments = listOf(navArgument("resourceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resourceId = backStackEntry.arguments?.getString("resourceId") ?: ""
            TurfBookingScreen(
                resourceId = resourceId,
                onBackClick = { navController.popBackStack() },
                onBookingSuccess = { navController.navigate(Screen.MyBookings.route) }
            )
        }

        composable(Screen.MyBookings.route) {
            BookingScreen(
                onTicketClick = { bookingId ->
                    navController.navigate(Screen.Ticket.route(bookingId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Booking.route) {
            BookingScreen(
                onTicketClick = { bookingId ->
                    navController.navigate(Screen.Ticket.route(bookingId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Ticket.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingScreen(
                selectedBookingId = bookingId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToMyBookings = {
                    navController.navigate(Screen.MyBookings.route)
                },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0)
                    }
                },
                onDebugClick = {
                    navController.navigate(Screen.Debug.route)
                }
            )
        }

        composable(Screen.Category.route) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("categoryName") ?: ""
            // Already handled in HomeScreen onCategoryClick, but keeping for direct deep links if any
            when {
                category.contains("Event", ignoreCase = true) ||
                category.contains("Concert", ignoreCase = true) -> {
                    val eventViewModel: EventViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return EventViewModel(EntryMySlotApp.instance.appContainer.eventRepository) as T
                            }
                        }
                    )
                    val uiState by eventViewModel.uiState.collectAsState()
                    EventsScreen(events = uiState.events, onBackClick = { navController.popBackStack() })
                }
                category.contains("Movie", ignoreCase = true) -> {
                    // Navigate to Movies directly to avoid loop
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Movies.route) {
                            popUpTo(Screen.Category.route) { inclusive = true }
                        }
                    }
                }
                else -> {
                    val eventViewModel: EventViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return EventViewModel(EntryMySlotApp.instance.appContainer.eventRepository) as T
                            }
                        }
                    )
                    val uiState by eventViewModel.uiState.collectAsState()
                    EventsScreen(events = uiState.events, onBackClick = { navController.popBackStack() })
                }
            }
        }

        composable(Screen.ListYourVenue.route) {
            ProfileScreen(
                showListYourVenue = true,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Debug.route) {
            TestScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
