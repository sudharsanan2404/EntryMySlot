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
import com.entrymyslot.app.screens.booking.BookingViewModel
import com.entrymyslot.app.screens.profile.ProfileViewModel

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

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
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
