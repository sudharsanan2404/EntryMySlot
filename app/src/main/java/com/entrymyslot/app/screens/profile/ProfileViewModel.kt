package com.entrymyslot.app.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.auth.AuthRepository
import com.entrymyslot.app.data.auth.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

data class TestUiState(
    val runningAction: String? = null,
    val lastResult: String = "Select an API test",
    val isSuccess: Boolean? = null
)

class ProfileViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _testState = MutableStateFlow(TestUiState())
    val testState: StateFlow<TestUiState> = _testState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null)
            repository.getMe()
                .onSuccess { user ->
                    _profileState.value = ProfileUiState(isLoading = false, user = user)
                }
                .onFailure { error ->
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        errorMessage = error.userMessage()
                    )
                }
        }
    }

    fun logout(onFinished: () -> Unit) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoggingOut = true)
            repository.logout()
            _profileState.value = ProfileUiState(isLoading = false)
            onFinished()
        }
    }

    fun testHealth() = runTest("Health") {
        repository.healthReady().getOrThrow().let {
            "status=${it.status}, database=${it.checks?.db?.status}, redis=${it.checks?.redis?.status}"
        }
    }

    fun testRegister(email: String, username: String, password: String) = runTest("Register OTP") {
        repository.register(email, username, password).getOrThrow().message
    }

    fun testVerifyOtp(email: String, otp: String) = runTest("Verify OTP") {
        val data = repository.verifyRegistrationOtp(email, otp).getOrThrow()
        "verified userId=${data.user.id}, email=${data.user.email}"
    }

    fun testResendOtp(email: String) = runTest("Resend OTP") {
        repository.resendRegistrationOtp(email).getOrThrow().message
    }

    fun testLogin(email: String, password: String) = runTest("Login") {
        val data = repository.login(email, password).getOrThrow()
        "logged in userId=${data.user.id}, sessionId=${data.sessionId}"
    }

    fun testGetMe() = runTest("Get Me") {
        val user = repository.getMe().getOrThrow()
        "userId=${user.id}, username=${user.username}, email=${user.email}"
    }

    fun testRefresh() = runTest("Refresh Token") {
        repository.refreshToken().getOrThrow()
        "access and refresh tokens rotated and saved securely"
    }

    fun testForgotPassword(email: String) = runTest("Forgot Password") {
        repository.forgotPassword(email).getOrThrow()
    }

    fun testResetPassword(token: String, newPassword: String) = runTest("Reset Password") {
        repository.resetPassword(token, newPassword).getOrThrow()
    }

    fun testLogout() = runTest("Logout") {
        repository.logout().getOrThrow()
        "server session revoked; local tokens cleared"
    }

    fun testLogoutAll() = runTest("Logout All") {
        repository.logoutAll().getOrThrow()
        "all server sessions revoked; local tokens cleared"
    }

    private fun runTest(action: String, block: suspend () -> String) {
        if (_testState.value.runningAction != null) return
        viewModelScope.launch {
            _testState.value = TestUiState(runningAction = action, lastResult = "Calling $action...")
            _testState.value = try {
                TestUiState(lastResult = "$action SUCCESS\n${block()}", isSuccess = true)
            } catch (error: Throwable) {
                TestUiState(lastResult = "$action FAILED\n${error.userMessage()}", isSuccess = false)
            }
        }
    }

    private fun Throwable.userMessage(): String =
        message?.takeIf { it.isNotBlank() } ?: "Network/server error"
}
