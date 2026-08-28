package com.entrymyslot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.entrymyslot.app.screens.auth.AuthScreen
import com.entrymyslot.app.screens.home.HomeScreen
import com.entrymyslot.app.screens.turf.TurfScreen
import com.entrymyslot.app.screens.turf.TurfBookingScreen
import com.entrymyslot.app.screens.movies.CinemaSelectionScreen
import com.entrymyslot.app.screens.movies.MovieBookingScreen
import com.entrymyslot.app.screens.movies.Cinema
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentScreen by remember { mutableStateOf("home") }
            var selectedSportId by remember { mutableStateOf("sport_1") }
            var selectedMovieId by remember { mutableStateOf("mov_1") }
            
            var selectedCinema by remember { mutableStateOf<Cinema?>(null) }
            var selectedTime by remember { mutableStateOf("") }
            var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    when (targetState) {
                        "movies", "cinema_selection" -> {
                            (fadeIn(animationSpec = tween(600)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(600)))
                                .togetherWith(fadeOut(animationSpec = tween(600)))
                        }
                        "auth", "turf", "turf_booking", "movie_booking" -> {
                            (fadeIn(animationSpec = tween(500)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start))
                                .togetherWith(fadeOut(animationSpec = tween(500)))
                        }
                        else -> {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    }
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "home" -> HomeScreen(
                        onAuthClick = { currentScreen = "auth" },
                        onEventClick = { /* Not used for navigation now */ },
                        onSportClick = { event ->
                            selectedSportId = event.id
                            currentScreen = "turf"
                        },
                        onMovieBookClick = { movie ->
                            selectedMovieId = movie.id
                            currentScreen = "cinema_selection"
                        }
                    )
                    "auth" -> AuthScreen(onBackClick = { currentScreen = "home" })
                    "turf" -> TurfScreen(
                        onBackClick = { currentScreen = "home" },
                        onBookNowClick = { currentScreen = "turf_booking" },
                        sportId = selectedSportId
                    )
                    "turf_booking" -> TurfBookingScreen(
                        onBackClick = { currentScreen = "turf" },
                        onContinueClick = { /* Handle payment */ }
                    )
                    "cinema_selection" -> CinemaSelectionScreen(
                        onBackClick = { currentScreen = "home" },
                        onTimeSelected = { cinema, time, date ->
                            selectedCinema = cinema
                            selectedTime = time
                            selectedDate = date
                            currentScreen = "movie_booking"
                        }
                    )
                    "movie_booking" -> MovieBookingScreen(
                        cinema = selectedCinema!!,
                        initialTime = selectedTime,
                        selectedDate = selectedDate,
                        onBackClick = { currentScreen = "cinema_selection" },
                        onContinueClick = { /* Handle payment */ }
                    )
                }
            }
        }
    }
}