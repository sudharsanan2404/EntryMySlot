package com.entrymyslot.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.entrymyslot.app.screens.movies.MoviesListScreen
import com.entrymyslot.app.screens.movies.sampleCinemas
import com.entrymyslot.app.screens.payment.BookingCategory
import com.entrymyslot.app.screens.payment.BookingDetails
import com.entrymyslot.app.screens.payment.PaymentScreen
import com.entrymyslot.app.screens.turf.TurfScreen
import com.entrymyslot.app.screens.turf.TurfBookingScreen
import com.entrymyslot.app.screens.turf.SportsListScreen
import com.entrymyslot.app.screens.events.EventsListScreen
import com.entrymyslot.app.screens.home.HomeViewModel
import com.entrymyslot.app.screens.home.PopularEvent
import com.entrymyslot.app.screens.home.toPopularEvent
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavigation(
    authTokenStore: AuthTokenStore
) {

    val navController = rememberNavController()
    val accessToken by authTokenStore.accessToken.collectAsState(initial = "LOADING")

    if (accessToken == "LOADING") {
        // You could show a splash screen here
        return
    }

    val homeViewModel: HomeViewModel = viewModel()
    val homeState by homeViewModel.uiState.collectAsState()

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
                    navController.navigate("cinema_selection")
                },
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
                        "Wallet" -> navController.navigate("wallet")
                        "Profile" -> navController.navigate("profile")
                    }
                }
            )
        }

        composable("movies_list") {
            MoviesListScreen(
                movies = homeState.movies.map { it.toPopularEvent() },
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie ->
                    // For now, movie click goes to cinema selection
                    navController.navigate("cinema_selection")
                }
            )
        }

        composable("sports_list") {
            SportsListScreen(
                sports = homeState.sports.map { it.toPopularEvent() },
                onBackClick = { navController.popBackStack() },
                onSportClick = { sport ->
                    navController.navigate("turf_details/${sport.id}")
                }
            )
        }

        composable("events_list") {
            EventsListScreen(
                title = "Upcoming Events",
                events = homeState.events.map { it.toPopularEvent() },
                onBackClick = { navController.popBackStack() },
                onEventClick = { event ->
                    navController.navigate("payment/EVENT/${event.title}")
                }
            )
        }

        composable("concerts_list") {
            EventsListScreen(
                title = "Music Concerts",
                events = homeState.events.filter { it.title.contains("Live", ignoreCase = true) || it.title.contains("Music", ignoreCase = true) }.map { it.toPopularEvent() },
                onBackClick = { navController.popBackStack() },
                onEventClick = { event ->
                    navController.navigate("payment/EVENT/${event.title}")
                }
            )
        }

        composable("bookings") {
            // Placeholder for Bookings
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("My Bookings Screen", color = Color.White)
            }
        }

        composable("wallet") {
            // Placeholder for Wallet
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Wallet Screen", color = Color.White)
            }
        }

        composable("profile") {
            // Placeholder for Profile
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile Screen", color = Color.White)
            }
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
                category = if (type == "TURF") BookingCategory.TURF else BookingCategory.MOVIE,
                date = "28 Aug 2026",
                time = "1:30 PM",
                location = if (type == "TURF") "Adyar, Chennai" else "PVR Cinemas, Chennai",
                details = if (type == "TURF") "Slots: 6 PM - 7 PM" else "Seats: A3, A4"
            )

            PaymentScreen(
                bookingDetails = details,
                onBackClick = { navController.popBackStack() },
                onPaySuccess = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}