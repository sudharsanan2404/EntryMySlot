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
    val isLoggedIn: Boolean = false,
    val passwordResetRequested: Boolean = false,
    val passwordResetComplete: Boolean = false
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
                
            !password.contains(Regex("[A-Z]")) ->
                setError("Must contain at least one uppercase letter")
                
            !password.contains(Regex("[a-z]")) ->
                setError("Must contain at least one lowercase letter")
                
            !password.contains(Regex("[0-9]")) ->
                setError("Must contain at least one number")
                
            !password.contains(Regex("[!@#\$%^&*(),.?\":{}|<>]")) ->
                setError("Must contain at least one special character")

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

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            setError("Email is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            repository.forgotPassword(email)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        passwordResetRequested = true,
                        passwordResetComplete = false,
                        successMessage = message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = getErrorMessage(error))
                }
        }
    }

    fun resetPassword(tokenOrLink: String, newPassword: String, confirmPassword: String) {
        val token = extractResetToken(tokenOrLink)
        when {
            token.isBlank() -> setError("Paste the reset link or token from your email")
            newPassword.length < 8 -> setError("Password must be at least 8 characters")
            !newPassword.contains(Regex("[A-Z]")) -> setError("Must contain at least one uppercase letter")
            !newPassword.contains(Regex("[a-z]")) -> setError("Must contain at least one lowercase letter")
            !newPassword.contains(Regex("[0-9]")) -> setError("Must contain at least one number")
            !newPassword.contains(Regex("[!@#\$%^&*(),.?\":{}|<>]")) -> setError("Must contain at least one special character")
            newPassword != confirmPassword -> setError("Passwords do not match")
            else -> viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
                repository.resetPassword(token, newPassword)
                    .onSuccess { message ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            passwordResetComplete = true,
                            successMessage = message
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = getErrorMessage(error))
                    }
            }
        }
    }

    fun clearPasswordRecovery() {
        _uiState.value = _uiState.value.copy(
            passwordResetRequested = false,
            passwordResetComplete = false,
            errorMessage = null,
            successMessage = null
        )
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
        val message = error.message ?: ""
        
        // Try to see if it's a JSON error message from our backend
        if (message.contains("\"message\":\"")) {
            try {
                val json = org.json.JSONObject(message)
                return json.optString("message", "An unexpected error occurred")
            } catch (e: Exception) {
                // Fallback to manual parsing if JSON parsing fails
            }
        }

        return when {
            message.contains("401") || message.contains("Unauthorized") || message.contains("Invalid email or password") -> 
                "Invalid email or password"
            message.contains("409") || message.contains("Conflict") || message.contains("already exists") -> 
                "User with this email already exists"
            message.contains("400") || message.contains("Bad Request") -> 
                "Invalid request. Please check your inputs"
            message.contains("timeout") || message.contains("Connection") || message.contains("Unable to resolve host") -> 
                "Network error. Please check your internet connection"
            message.contains("404") -> 
                "Server endpoint not found"
            message.contains("500") || message.contains("Internal Server Error") ->
                "Server is currently undergoing maintenance. Please try later"
            else -> message.takeIf { it.isNotBlank() && it.length < 50 } ?: "An unexpected error occurred. Please try again"
        }
    }

    private fun extractResetToken(value: String): String {
        val trimmed = value.trim()
        val raw = if (trimmed.contains("token=")) {
            trimmed.substringAfter("token=").substringBefore('&').substringBefore('#')
        } else {
            trimmed
        }
        return runCatching {
            java.net.URLDecoder.decode(raw, Charsets.UTF_8.name())
        }.getOrDefault(raw)
    }
}
