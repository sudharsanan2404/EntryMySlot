package com.entrymyslot.app.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isOtpMode: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthScreenViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var registeredEmail: String = ""

    // ------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------

    fun login(
        email: String,
        password: String
    ) {
        if (email.isBlank()) {
            setError("Email is required")
            return
        }

        if (password.isBlank()) {
            setError("Password is required")
            return
        }

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            repository.login(
                email = email,
                password = password
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        successMessage = "Login successful"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = getErrorMessage(error)
                    )
                }
        }
    }

    // ------------------------------------------------------------
    // REGISTER → REQUEST OTP
    // ------------------------------------------------------------

    fun register(
        email: String,
        fullName: String,
        password: String,
        confirmPassword: String
    ) {
        when {
            fullName.isBlank() ->
                setError("Full name is required")

            email.isBlank() ->
                setError("Email is required")

            password.length < 8 ->
                setError("Password must be at least 8 characters")

            password != confirmPassword ->
                setError("Passwords do not match")

            else -> {
                viewModelScope.launch {

                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        errorMessage = null,
                        successMessage = null
                    )

                    // Backend expects "username".
                    // We use the entered full name as username.
                    repository.register(
                        email = email,
                        username = fullName,
                        password = password
                    )
                        .onSuccess {
                            registeredEmail = email.trim()

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isOtpMode = true,
                                successMessage = it.message
                            )
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = getErrorMessage(error)
                            )
                        }
                }
            }
        }
    }

    // ------------------------------------------------------------
    // VERIFY OTP
    // ------------------------------------------------------------

    fun verifyOtp(
        email: String,
        otp: String
    ) {
        if (otp.length != 6) {
            setError("Enter the 6-digit OTP")
            return
        }

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            repository.verifyRegistrationOtp(
                email = email,
                otp = otp
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isOtpMode = false,
                        isLoggedIn = true,
                        successMessage = it.let {
                            "Account created successfully"
                        }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = getErrorMessage(error)
                    )
                }
        }
    }

    // ------------------------------------------------------------
    // RESEND OTP
    // ------------------------------------------------------------

    fun resendOtp(email: String = registeredEmail) {
        if (email.isBlank()) {
            setError("Email is required")
            return
        }

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            repository.resendRegistrationOtp(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = it.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = getErrorMessage(error)
                    )
                }
        }
    }

    // ------------------------------------------------------------
    // LOGOUT
    // ------------------------------------------------------------

    fun logout() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            repository.logout()

            _uiState.value = AuthUiState(
                isLoading = false
            )
        }
    }

    // ------------------------------------------------------------
    // UI helpers
    // ------------------------------------------------------------

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun clearOtpMode() {
        _uiState.value = _uiState.value.copy(
            isOtpMode = false,
            errorMessage = null,
            successMessage = null
        )
    }

    private fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message,
            successMessage = null
        )
    }

    private fun getErrorMessage(error: Throwable): String {

        android.util.Log.e(
            "AUTH_DEBUG",
            "Auth Error",
            error
        )

        return buildString {
            append(error::class.java.simpleName)
            append("\n")
            append(error.message ?: "Unknown error")
        }
    }
}