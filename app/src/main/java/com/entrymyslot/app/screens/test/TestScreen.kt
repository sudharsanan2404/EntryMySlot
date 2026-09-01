package com.entrymyslot.app.screens.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.booking.MovieSeatHoldRequest
import com.entrymyslot.app.data.booking.TurfHoldRequest
import com.entrymyslot.app.screens.booking.BookingViewModel
import com.entrymyslot.app.screens.events.EventViewModel
import com.entrymyslot.app.screens.movies.MovieViewModel
import com.entrymyslot.app.screens.profile.ProfileViewModel
import com.entrymyslot.app.screens.turf.TurfViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(onBackClick: () -> Unit) {
    val app = LocalContext.current.applicationContext as EntryMySlotApp
    val viewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(app.appContainer.authRepository) as T
        }
    )
    val state by viewModel.testState.collectAsStateWithLifecycle()
    val bookingViewModel: BookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BookingViewModel(
                    bookingApi = app.appContainer.bookingApi,
                    networkMonitor = app.appContainer.networkMonitor
                ) as T
        }
    )
    val bookingState by bookingViewModel.uiState.collectAsStateWithLifecycle()
    val movieViewModel: MovieViewModel = viewModel(
        key = "details_test_movie",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MovieViewModel(app.appContainer.detailsApi, app.appContainer.networkMonitor) as T
        }
    )
    val movieState by movieViewModel.uiState.collectAsStateWithLifecycle()
    val eventViewModel: EventViewModel = viewModel(
        key = "details_test_event",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EventViewModel(app.appContainer.detailsApi, app.appContainer.networkMonitor) as T
        }
    )
    val eventState by eventViewModel.uiState.collectAsStateWithLifecycle()
    val turfViewModel: TurfViewModel = viewModel(
        key = "details_test_turf",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TurfViewModel(app.appContainer.detailsApi, app.appContainer.networkMonitor) as T
        }
    )
    val turfState by turfViewModel.uiState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var movieId by remember { mutableStateOf("12") }
    var eventId by remember { mutableStateOf("10") }
    var turfResourceId by remember { mutableStateOf("1") }
    var movieTestRun by remember { mutableStateOf(false) }
    var eventTestRun by remember { mutableStateOf(false) }
    var turfTestRun by remember { mutableStateOf(false) }
    var bookingApiRunning by remember { mutableStateOf(false) }
    var bookingApiResult by remember { mutableStateOf("Booking hold tests not run") }
    var showtimeId by remember { mutableStateOf("") }
    var movieSeatIds by remember { mutableStateOf("") }
    var movieHoldKey by remember { mutableStateOf<String?>(null) }
    var turfUnitId by remember { mutableStateOf("") }
    var turfHoldToken by remember { mutableStateOf<String?>(null) }
    val testScope = rememberCoroutineScope()
    val enabled = state.runningAction == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EntryMySlot API Test") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Auth API inputs", style = MaterialTheme.typography.titleMedium)
            Text("Latest test result", style = MaterialTheme.typography.labelLarge)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        state.runningAction != null -> Color(0xFFFFF4CE)
                        state.isSuccess == true -> Color(0xFFE6F4EA)
                        state.isSuccess == false -> Color(0xFFFCE8E6)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    text = when {
                        state.runningAction != null ->
                            "${state.runningAction} RUNNING\n${state.lastResult}"
                        else -> state.lastResult
                    },
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF202124)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username / Full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                label = { Text("Registration OTP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TestButton("Health / DB / Redis", enabled) { viewModel.testHealth() }
            TestButton(
                text = "My Bookings API",
                enabled = enabled && !bookingState.isLoading
            ) {
                bookingViewModel.loadBookings()
            }
            Text("My Bookings API result", style = MaterialTheme.typography.labelLarge)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        bookingState.isLoading -> Color(0xFFFFF4CE)
                        bookingState.errorMessage != null -> Color(0xFFFCE8E6)
                        bookingState.hasLoaded -> Color(0xFFE6F4EA)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    text = when {
                        bookingState.isLoading -> "My Bookings RUNNING"
                        bookingState.errorMessage != null ->
                            "My Bookings FAILED\n${bookingState.errorMessage}"
                        bookingState.hasLoaded ->
                            "My Bookings SUCCESS\n${bookingState.total} upcoming booking(s) returned"
                        else -> "My Bookings test not run"
                    },
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF202124)
                )
            }
            Text("Details API tests", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = movieId,
                onValueChange = { movieId = it.filter(Char::isDigit) },
                label = { Text("Movie ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TestButton("Movie Details API", movieId.isNotBlank() && (!movieTestRun || !movieState.isLoading)) {
                movieTestRun = true
                movieViewModel.loadMovie(movieId)
            }
            DetailsTestResultCard(
                label = "Movie Details",
                hasRun = movieTestRun,
                isLoading = movieState.isLoading,
                httpStatus = movieState.httpStatus,
                errorMessage = movieState.errorMessage,
                successSummary = movieState.movie?.let { "ID ${it.id} · ${it.title}" }
            )

            OutlinedTextField(
                value = eventId,
                onValueChange = { eventId = it.filter(Char::isDigit) },
                label = { Text("Event ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TestButton("Event Details API", eventId.isNotBlank() && (!eventTestRun || !eventState.isLoading)) {
                eventTestRun = true
                eventViewModel.loadEvent(eventId)
            }
            DetailsTestResultCard(
                label = "Event Details",
                hasRun = eventTestRun,
                isLoading = eventState.isLoading,
                httpStatus = eventState.httpStatus,
                errorMessage = eventState.errorMessage,
                successSummary = eventState.event?.let { "ID ${it.id} · ${it.title}" }
            )

            OutlinedTextField(
                value = turfResourceId,
                onValueChange = { turfResourceId = it.filter(Char::isDigit) },
                label = { Text("Turf resource ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TestButton("Turf Details API", turfResourceId.isNotBlank() && (!turfTestRun || !turfState.isLoading)) {
                turfTestRun = true
                turfViewModel.loadTurf(turfResourceId)
            }
            DetailsTestResultCard(
                label = "Turf Details",
                hasRun = turfTestRun,
                isLoading = turfState.isLoading,
                httpStatus = turfState.httpStatus,
                errorMessage = turfState.errorMessage,
                successSummary = turfState.turf?.let { "Resource ${it.id} · ${it.title}" }
            )

            Text("Booking availability and hold APIs", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (bookingApiRunning) Color(0xFFFFF4CE) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(bookingApiResult, modifier = Modifier.padding(16.dp), color = Color(0xFF202124))
            }
            OutlinedTextField(
                value = showtimeId,
                onValueChange = { showtimeId = it.filter(Char::isDigit) },
                label = { Text("Movie showtime ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = movieSeatIds,
                onValueChange = { movieSeatIds = it.filter { ch -> ch.isDigit() || ch == ',' || ch == ' ' } },
                label = { Text("Movie seat IDs, comma separated") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TwoTestButtons(
                leftText = "Movie Showtimes",
                rightText = "Movie Seat Layout",
                enabled = !bookingApiRunning && movieId.isNotBlank(),
                onLeft = {
                    testScope.launch {
                        bookingApiRunning = true
                        bookingApiResult = runBookingApiTest("Movie Showtimes") {
                            val response = app.appContainer.bookingApi.getMovieShowtimes(movieId.toInt())
                            response.data.firstOrNull()?.let { showtimeId = it.id.toString() }
                            "HTTP 200 · ${response.data.size} showtime(s); first ID saved"
                        }
                        bookingApiRunning = false
                    }
                },
                onRight = {
                    testScope.launch {
                        bookingApiRunning = true
                        bookingApiResult = runBookingApiTest("Movie Seat Layout") {
                            val response = app.appContainer.bookingApi.getMovieSeatLayout(showtimeId.toInt())
                            val seats = response.data.rows.flatMap { it.seats }
                            movieSeatIds = seats.filter { it.status == "available" }.take(2).joinToString(",") { it.seatId.toString() }
                            "HTTP 200 · ${response.data.rows.size} row(s), ${seats.size} seat(s); two available IDs saved"
                        }
                        bookingApiRunning = false
                    }
                }
            )
            TwoTestButtons(
                leftText = "Hold Movie Seats",
                rightText = "Release Movie Hold",
                enabled = !bookingApiRunning && showtimeId.isNotBlank(),
                onLeft = {
                    testScope.launch {
                        bookingApiRunning = true
                        bookingApiResult = runBookingApiTest("Movie Hold") {
                            val ids = movieSeatIds.split(',').mapNotNull { it.trim().toIntOrNull() }
                            val response = app.appContainer.bookingApi.holdMovieSeats(
                                MovieSeatHoldRequest(showtimeId.toInt(), ids, movieHoldKey)
                            )
                            movieHoldKey = response.data.holdKey
                            "HTTP 200 · ${response.data.heldSeatIds.size} seat(s) held for ${response.data.ttlSeconds}s"
                        }
                        bookingApiRunning = false
                    }
                },
                onRight = {
                    testScope.launch {
                        bookingApiRunning = true
                        bookingApiResult = runBookingApiTest("Movie Hold Release") {
                            val key = requireNotNull(movieHoldKey) { "Run Movie Hold first" }
                            app.appContainer.bookingApi.releaseMovieSeats(key)
                            movieHoldKey = null
                            "HTTP 200 · hold released"
                        }
                        bookingApiRunning = false
                    }
                }
            )
            TestButton("Movie Hold Status", !bookingApiRunning && movieHoldKey != null) {
                testScope.launch {
                    bookingApiRunning = true
                    bookingApiResult = runBookingApiTest("Movie Hold Status") {
                        val response = app.appContainer.bookingApi.getMovieHoldStatus(requireNotNull(movieHoldKey))
                        "HTTP 200 · active=${response.data.active}, seats=${response.data.seatIds.size}, remaining=${response.data.ttlSeconds}s"
                    }
                    bookingApiRunning = false
                }
            }
            TestButton("Event Zones API", !bookingApiRunning && eventId.isNotBlank()) {
                testScope.launch {
                    bookingApiRunning = true
                    bookingApiResult = runBookingApiTest("Event Zones") {
                        val response = app.appContainer.bookingApi.getEventZones(eventId)
                        "HTTP 200 · ${response.zones.size} zone(s); empty means General Admission"
                    }
                    bookingApiRunning = false
                }
            }
            OutlinedTextField(
                value = turfUnitId,
                onValueChange = { turfUnitId = it.filter(Char::isDigit) },
                label = { Text("Turf availability unit ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TestButton("Turf Availability API", !bookingApiRunning && turfResourceId.isNotBlank()) {
                testScope.launch {
                    bookingApiRunning = true
                    bookingApiResult = runBookingApiTest("Turf Availability") {
                        val response = app.appContainer.bookingApi.getTurfAvailability(turfResourceId, LocalDate.now().toString())
                        response.data.slots.firstOrNull { it.status == "available" }?.let { turfUnitId = it.unit_id.toString() }
                        "HTTP 200 · ${response.data.slots.size} slot(s), ${response.data.summary.available} available; first unit saved"
                    }
                    bookingApiRunning = false
                }
            }
            TwoTestButtons(
                leftText = "Hold Turf Slot",
                rightText = "Release Turf Hold",
                enabled = !bookingApiRunning && turfUnitId.isNotBlank(),
                onLeft = {
                    testScope.launch {
                        bookingApiRunning = true
                        bookingApiResult = runBookingApiTest("Turf Hold") {
                            val response = app.appContainer.bookingApi.holdTurfSlot(TurfHoldRequest(turfUnitId.toInt()))
                            turfHoldToken = response.data.token
                            "HTTP 201 · unit ${response.data.unitId} held for five minutes"
                        }
                        bookingApiRunning = false
                    }
                },
                onRight = {
                    testScope.launch {
                        bookingApiRunning = true
                        bookingApiResult = runBookingApiTest("Turf Hold Release") {
                            val token = requireNotNull(turfHoldToken) { "Run Turf Hold first" }
                            val response = app.appContainer.bookingApi.releaseTurfHold(token)
                            turfHoldToken = null
                            "HTTP 200 · ${response.data.reason}"
                        }
                        bookingApiRunning = false
                    }
                }
            )
            TestButton("Turf Hold Status", !bookingApiRunning && turfHoldToken != null) {
                testScope.launch {
                    bookingApiRunning = true
                    bookingApiResult = runBookingApiTest("Turf Hold Status") {
                        val response = app.appContainer.bookingApi.getTurfHoldStatus(requireNotNull(turfHoldToken))
                        "HTTP 200 · active=${response.data.active}, unit=${response.data.unitId}, remaining=${response.data.ttlSeconds}s"
                    }
                    bookingApiRunning = false
                }
            }
            TwoTestButtons(
                leftText = "Register OTP",
                rightText = "Verify OTP",
                enabled = enabled,
                onLeft = { viewModel.testRegister(email, username, password) },
                onRight = { viewModel.testVerifyOtp(email, otp) }
            )
            TwoTestButtons(
                leftText = "Resend OTP",
                rightText = "Login",
                enabled = enabled,
                onLeft = { viewModel.testResendOtp(email) },
                onRight = { viewModel.testLogin(email, password) }
            )
            TwoTestButtons(
                leftText = "Get Me",
                rightText = "Refresh Token",
                enabled = enabled,
                onLeft = { viewModel.testGetMe() },
                onRight = { viewModel.testRefresh() }
            )
            TestButton("Forgot Password", enabled) { viewModel.testForgotPassword(email) }

            OutlinedTextField(
                value = resetToken,
                onValueChange = { resetToken = it },
                label = { Text("Reset token from email link") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TestButton("Reset Password", enabled) {
                viewModel.testResetPassword(resetToken, newPassword)
            }
            TwoTestButtons(
                leftText = "Logout",
                rightText = "Logout All",
                enabled = enabled,
                onLeft = { viewModel.testLogout() },
                onRight = { viewModel.testLogoutAll() }
            )

        }
    }
}

private suspend fun runBookingApiTest(label: String, block: suspend () -> String): String =
    try {
        "$label SUCCESS\n${block()}"
    } catch (error: Throwable) {
        val status = (error as? HttpException)?.code()
        buildString {
            append("$label FAILED")
            status?.let { append(" · HTTP $it") }
            append("\n")
            append(error.message ?: "Unknown error")
        }
    }

@Composable
private fun DetailsTestResultCard(
    label: String,
    hasRun: Boolean,
    isLoading: Boolean,
    httpStatus: Int?,
    errorMessage: String?,
    successSummary: String?
) {
    val resultText = when {
        !hasRun -> "$label test not run"
        isLoading -> "$label RUNNING"
        successSummary != null -> buildString {
            append("$label SUCCESS")
            httpStatus?.let { append(" · HTTP $it") }
            append("\n$successSummary")
        }
        else -> buildString {
            append("$label FAILED")
            httpStatus?.let { append(" · HTTP $it") }
            append("\n${errorMessage ?: "Unknown error"}")
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                hasRun && isLoading -> Color(0xFFFFF4CE)
                successSummary != null -> Color(0xFFE6F4EA)
                hasRun -> Color(0xFFFCE8E6)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = resultText,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF202124)
        )
    }
}

@Composable
private fun TestButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text(text)
    }
}

@Composable
private fun TwoTestButtons(
    leftText: String,
    rightText: String,
    enabled: Boolean,
    onLeft: () -> Unit,
    onRight: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onLeft, enabled = enabled, modifier = Modifier.weight(1f)) {
            Text(leftText)
        }
        Button(onClick = onRight, enabled = enabled, modifier = Modifier.weight(1f)) {
            Text(rightText)
        }
    }
}
