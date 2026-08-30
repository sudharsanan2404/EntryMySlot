package com.entrymyslot.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.entrymyslot.app.core.storage.AuthTokenStore
import com.entrymyslot.app.screens.auth.AuthScreen
import com.entrymyslot.app.screens.home.HomeScreen
import com.entrymyslot.app.screens.movies.CinemaSelectionScreen
import com.entrymyslot.app.screens.movies.MovieBookingScreen
import com.entrymyslot.app.screens.movies.MovieDetailsScreen
import com.entrymyslot.app.screens.movies.MoviesListScreen
import com.entrymyslot.app.screens.movies.sampleCinemas
import com.entrymyslot.app.screens.payment.BookingCategory
import com.entrymyslot.app.screens.payment.BookingDetails
import com.entrymyslot.app.screens.payment.PaymentScreen
import com.entrymyslot.app.screens.turf.TurfScreen
import com.entrymyslot.app.screens.turf.TurfBookingScreen
import com.entrymyslot.app.screens.turf.SportsListScreen
import com.entrymyslot.app.screens.events.EventsListScreen
import com.entrymyslot.app.screens.events.EventBookingScreen
import com.entrymyslot.app.screens.home.HomeViewModel
import com.entrymyslot.app.screens.home.PopularEvent
import com.entrymyslot.app.screens.home.toPopularEvent
import com.entrymyslot.app.screens.home.latestMovies
import com.entrymyslot.app.screens.home.sportsNearYou
import com.entrymyslot.app.screens.home.popularEvents
import com.entrymyslot.app.screens.profile.ProfileScreen
import com.entrymyslot.app.screens.booking.BookingScreen
import com.entrymyslot.app.screens.booking.upcomingBookings
import com.entrymyslot.app.screens.booking.pastBookings
import com.entrymyslot.app.screens.search.SearchResultType
import com.entrymyslot.app.screens.search.SearchScreen
import com.entrymyslot.app.screens.ticket.TicketDetails
import com.entrymyslot.app.screens.ticket.TicketScreen
import androidx.lifecycle.viewmodel.compose.viewModel
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

    val homeViewModel: HomeViewModel = viewModel()
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = if (accessToken != null) "home" else "auth"
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
                onSportClick = { sport ->
                    navController.navigate("turf_details/${sport.id}")
                },
                onMovieBookClick = {
                    navController.navigate("movie_details/${it.id}")
                },
                onSearchClick = { navController.navigate("search") },
                homeViewModel = homeViewModel,
                onCategoryClick = { category ->
                    when (category) {
                        "Movies" -> navController.navigate("movies_list")
                        "Sports" -> navController.navigate("sports_list")
                        "Events" -> navController.navigate("events_list")
                        "Concerts" -> navController.navigate("concerts_list")
                    }
                },
                onBottomNavigationClick = { item ->
                    when (item) {
                        "My Bookings" -> navController.navigate("bookings")
                        "Profile" -> navController.navigate("profile")
                        "Search" -> navController.navigate("search")
                    }
                }
            )
        }

        composable("movies_list") {
            MoviesListScreen(
                movies = homeState.movies.map { it.toPopularEvent() }.ifEmpty { latestMovies },
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie ->
                    navController.navigate("movie_details/${movie.id}")
                }
            )
        }

        composable("search") {
            SearchScreen(
                movies = homeState.movies.map { it.toPopularEvent() }.ifEmpty { latestMovies },
                sports = homeState.sports.map { it.toPopularEvent() }.ifEmpty { sportsNearYou },
                events = homeState.events.map { it.toPopularEvent() }.ifEmpty { popularEvents },
                onBackClick = { navController.popBackStack() },
                onResultClick = { result ->
                    when (result.type) {
                        SearchResultType.MOVIE -> navController.navigate("movie_details/${result.item.id}")
                        SearchResultType.SPORT -> navController.navigate("turf_details/${result.item.id}")
                        SearchResultType.EVENT -> navController.navigate("event_booking/${result.item.id}")
                    }
                }
            )
        }

        composable("movie_details/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            val movies = homeState.movies.map { it.toPopularEvent() }.ifEmpty { latestMovies }
            val movie = movies.find { it.id == movieId } ?: movies.first()
            MovieDetailsScreen(
                movie = movie,
                onBackClick = { navController.popBackStack() },
                onBookClick = { navController.navigate("cinema_selection") }
            )
        }

        composable("sports_list") {
            SportsListScreen(
                sports = homeState.sports.map { it.toPopularEvent() }.ifEmpty { sportsNearYou },
                onBackClick = { navController.popBackStack() },
                onSportClick = { sport ->
                    navController.navigate("turf_details/${sport.id}")
                }
            )
        }

        composable("events_list") {
            EventsListScreen(
                title = "Upcoming Events",
                events = homeState.events.map { it.toPopularEvent() }.ifEmpty { popularEvents },
                onBackClick = { navController.popBackStack() },
                onEventClick = { event ->
                    navController.navigate("event_booking/${event.id}")
                }
            )
        }

        composable("concerts_list") {
            val concerts = homeState.events
                .filter { it.title.contains("Live", ignoreCase = true) || it.title.contains("Music", ignoreCase = true) }
                .map { it.toPopularEvent() }
                .ifEmpty { popularEvents.filter { it.title.contains("Live", ignoreCase = true) } }

            EventsListScreen(
                title = "Music Concerts",
                events = concerts,
                onBackClick = { navController.popBackStack() },
                onEventClick = { event ->
                    navController.navigate("event_booking/${event.id}")
                }
            )
        }

        composable("event_booking/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val allEvents = (homeState.events.map { it.toPopularEvent() } + popularEvents)
            val event = allEvents.find { it.id == eventId } ?: popularEvents[0]

            EventBookingScreen(
                event = event,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { _ ->
                    navController.navigate("payment/EVENT/${event.title}")
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
                        "Search" -> navController.navigate("search")
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
                        "Search" -> navController.navigate("search")
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

        composable("turf_details/{sportId}") { backStackEntry ->
            val sportId = backStackEntry.arguments?.getString("sportId") ?: "sport_1"
            TurfScreen(
                sportId = sportId,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = {
                    navController.navigate("turf_booking/$sportId")
                }
            )
        }

        composable("turf_booking/{sportId}") { backStackEntry ->
            val sportId = backStackEntry.arguments?.getString("sportId") ?: "sport_1"
            TurfBookingScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = {
                    navController.navigate("payment/TURF/$sportId")
                }
            )
        }

        composable("cinema_selection") {
            CinemaSelectionScreen(
                onBackClick = { navController.popBackStack() },
                onTimeSelected = { cinema, time, _ ->
                    navController.navigate("movie_booking/${cinema.id}/$time")
                }
            )
        }

        composable("movie_booking/{cinemaId}/{time}") { backStackEntry ->
            val cinemaId = backStackEntry.arguments?.getString("cinemaId")
            val time = backStackEntry.arguments?.getString("time") ?: ""
            val cinema = sampleCinemas.find { it.id == cinemaId } ?: sampleCinemas[0]

            MovieBookingScreen(
                cinema = cinema,
                initialTime = time,
                selectedDate = java.util.Calendar.getInstance(), 
                onBackClick = { navController.popBackStack() },
                onContinueClick = {
                    navController.navigate("payment/MOVIE/${cinema.name}")
                }
            )
        }

        composable("payment/{type}/{title}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "MOVIE"
            val title = backStackEntry.arguments?.getString("title") ?: "Booking"
            
            val details = BookingDetails(
                title = if (type == "TURF") "Green Arena Turf" else title,
                category = when (type) {
                    "TURF" -> BookingCategory.TURF
                    "EVENT" -> BookingCategory.EVENT
                    else -> BookingCategory.MOVIE
                },
                date = "28 Aug 2026",
                time = "1:30 PM",
                location = if (type == "TURF") "Adyar, Chennai" else "PVR Cinemas, Chennai",
                details = if (type == "TURF") "Slots: 6 PM - 7 PM" else "Seats: A3, A4"
            )

            PaymentScreen(
                bookingDetails = details,
                onBackClick = { navController.popBackStack() },
                onPaySuccess = {
                    navController.navigate("ticket/confirmed")
                }
            )
        }

        composable("ticket/{bookingId}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: "confirmed"
            val booking = (upcomingBookings + pastBookings).find { it.id == bookingId }
            val ticket = if (booking != null) {
                TicketDetails(
                    bookingId = "EMS-${booking.id.padStart(6, '0')}",
                    title = booking.title,
                    category = booking.type.name,
                    venue = booking.location,
                    date = booking.dateTime.substringBefore(" • "),
                    time = booking.dateTime.substringAfter(" • ", "Confirmed"),
                    admission = booking.details
                )
            } else {
                TicketDetails("EMS-260830", "Booking Confirmed", "Entry Pass", "EntryMySlot Venue", "30 Aug 2026", "Confirmed", "1 Guest")
            }
            TicketScreen(
                ticket = ticket,
                onBackClick = { navController.popBackStack() },
                onDoneClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                }
            )
        }
    }
}
