package com.entrymyslot.app.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isOtpMode: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val passwordResetRequested: Boolean = false,
    val passwordResetComplete: Boolean = false
)

/** Frontend-only auth state. Replace these actions when the production backend is integrated. */
class AuthScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var registeredEmail = ""

    fun login(email: String, password: String) {
        when {
            email.isBlank() -> setError("Email is required")
            password.isBlank() -> setError("Password is required")
            else -> complete("Login successful", loggedIn = true)
        }
    }

    fun register(email: String, fullName: String, password: String, confirmPassword: String) {
        when {
            fullName.isBlank() -> setError("Full name is required")
            email.isBlank() -> setError("Email is required")
            password.length < 8 -> setError("Password must be at least 8 characters")
            password != confirmPassword -> setError("Passwords do not match")
            else -> {
                registeredEmail = email.trim()
                viewModelScope.launch {
                    showLoading()
                    delay(450)
                    _uiState.value = AuthUiState(isOtpMode = true, successMessage = "Verification code ready")
                }
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        if (email.isBlank() || otp.length != 6) setError("Enter the 6-digit OTP")
        else complete("Account created successfully", loggedIn = true)
    }

    fun resendOtp(email: String = registeredEmail) {
        if (email.isBlank()) setError("Email is required") else complete("Verification code refreshed")
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) return setError("Email is required")
        viewModelScope.launch {
            showLoading(); delay(450)
            _uiState.value = AuthUiState(passwordResetRequested = true, successMessage = "Continue with the reset form")
        }
    }

    fun resetPassword(tokenOrLink: String, newPassword: String, confirmPassword: String) {
        when {
            tokenOrLink.isBlank() -> setError("Paste the reset link or token")
            newPassword.length < 8 -> setError("Password must be at least 8 characters")
            newPassword != confirmPassword -> setError("Passwords do not match")
            else -> viewModelScope.launch {
                showLoading(); delay(450)
                _uiState.value = AuthUiState(passwordResetComplete = true, successMessage = "Password updated")
            }
        }
    }

    fun clearPasswordRecovery() { _uiState.value = AuthUiState() }
    fun clearMessages() { _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null) }
    fun clearOtpMode() { _uiState.value = AuthUiState() }

    private fun complete(message: String, loggedIn: Boolean = false) {
        viewModelScope.launch {
            showLoading(); delay(450)
            _uiState.value = AuthUiState(successMessage = message, isLoggedIn = loggedIn)
        }
    }

    private fun showLoading() { _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null) }
    private fun setError(message: String) { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message, successMessage = null) }
}
