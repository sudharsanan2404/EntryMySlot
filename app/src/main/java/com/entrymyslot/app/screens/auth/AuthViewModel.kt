package com.entrymyslot.app.screens.auth

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.core.ApiResult
import androidx.lifecycle.viewModelScope
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

class AuthScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = (application as EntryMySlotApp).appContainer.backend.auth
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var registeredEmail = ""

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return
        when {
            email.isBlank() -> setError("Email is required")
            password.isBlank() -> setError("Password is required")
            else -> request({ auth.login(email.trim(), password) }) {
                AuthUiState(isLoggedIn = AuthScope.USER in auth.sessionState.value.authenticatedScopes)
            }
        }
    }

    fun register(email: String, fullName: String, password: String, confirmPassword: String) {
        if (_uiState.value.isLoading) return
        when {
            fullName.isBlank() -> setError("Full name is required")
            email.isBlank() -> setError("Email is required")
            password.length < 8 -> setError("Password must be at least 8 characters")
            password != confirmPassword -> setError("Passwords do not match")
            else -> {
                registeredEmail = email.trim()
                request({ auth.register(registeredEmail, password, fullName.trim()) }) {
                    AuthUiState(isOtpMode = true, successMessage = it.message)
                }
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        if (_uiState.value.isLoading) return
        if (email.isBlank() || otp.length != 6 || !otp.all { it in '0'..'9' }) setError("Enter the 6-digit OTP")
        else request({ auth.verifyRegistrationOtp(email.trim(), otp) }) {
            AuthUiState(isLoggedIn = AuthScope.USER in auth.sessionState.value.authenticatedScopes)
        }
    }

    fun resendOtp(email: String = registeredEmail) {
        if (_uiState.value.isLoading) return
        if (email.isBlank()) setError("Email is required") else request({ auth.resendRegistrationOtp(email.trim()) }) {
            _uiState.value.copy(isLoading = false, successMessage = it.message)
        }
    }

    fun forgotPassword(email: String) {
        if (_uiState.value.isLoading) return
        if (email.isBlank()) return setError("Email is required")
        request({ auth.forgotPassword(email.trim()) }) {
            AuthUiState(passwordResetRequested = true, successMessage = it.message)
        }
    }

    fun resetPassword(tokenOrLink: String, newPassword: String, confirmPassword: String) {
        if (_uiState.value.isLoading) return
        when {
            tokenOrLink.isBlank() -> setError("Paste the reset link or token")
            newPassword.length < 8 -> setError("Password must be at least 8 characters")
            newPassword != confirmPassword -> setError("Passwords do not match")
            else -> request({
                val input = tokenOrLink.trim()
                val token = if (input.startsWith("http://", ignoreCase = true) || input.startsWith("https://", ignoreCase = true)) {
                    Uri.parse(input).getQueryParameter("token").orEmpty()
                } else input
                if (token.isBlank()) ApiResult.UnexpectedError("The reset link does not contain a token.")
                else auth.resetPassword(token, newPassword)
            }) {
                AuthUiState(passwordResetComplete = true, successMessage = it.message)
            }
        }
    }

    fun clearPasswordRecovery() {
        if (!_uiState.value.isLoading) _uiState.value = AuthUiState()
    }
    fun clearMessages() { _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null) }

    private fun <T> request(call: suspend () -> ApiResult<T>, success: (ApiResult.Success<T>) -> AuthUiState) {
        if (_uiState.value.isLoading || _uiState.value.isLoggedIn) return
        showLoading()
        viewModelScope.launch {
            when (val result = call()) {
                is ApiResult.Success -> _uiState.value = success(result)
                is ApiResult.Failure -> setError(result.userMessage)
            }
        }
    }

    private fun showLoading() { _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null) }
    private fun setError(message: String) { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message, successMessage = null) }
}
