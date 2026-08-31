package com.entrymyslot.app.data.auth

import com.entrymyslot.app.core.storage.AuthTokenStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.Response

class ApiException(
    val statusCode: Int,
    override val message: String
) : Exception(message)

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStore: AuthTokenStore
) {
    private val errorJson = Json { ignoreUnknownKeys = true }

    suspend fun migrateStoredSession() = tokenStore.migrateLegacyTokens()

    suspend fun hasStoredSession(): Boolean =
        !tokenStore.refreshToken.firstOrNull().isNullOrBlank()

    suspend fun login(
        email: String,
        password: String,
        deviceInfo: String? = "Android"
    ): Result<LoginData> = apiCall {
        val response = authApi.login(LoginRequest(email.trim(), password, deviceInfo))
        val body = response.requireBody("Login failed")
        if (!body.success) throw ApiException(response.code(), "Login failed")
        tokenStore.saveTokens(body.data.tokens.accessToken, body.data.tokens.refreshToken)
        body.data
    }

    suspend fun register(
        email: String,
        username: String?,
        password: String
    ): Result<RegisterOtpResponse> = apiCall {
        authApi.register(
            RegisterRequest(email.trim(), username?.trim()?.ifBlank { null }, password)
        ).requireBody("Registration failed")
    }

    suspend fun verifyRegistrationOtp(
        email: String,
        otp: String,
        deviceInfo: String? = "Android"
    ): Result<VerifyOtpData> = apiCall {
        val response = authApi.verifyRegistrationOtp(
            VerifyOtpRequest(email.trim(), otp.trim(), deviceInfo)
        )
        val body = response.requireBody("OTP verification failed")
        if (!body.success) throw ApiException(response.code(), body.message)
        tokenStore.saveTokens(body.data.tokens.accessToken, body.data.tokens.refreshToken)
        body.data
    }

    suspend fun resendRegistrationOtp(email: String): Result<ResendOtpResponse> = apiCall {
        authApi.resendRegistrationOtp(ResendOtpRequest(email.trim()))
            .requireBody("Failed to resend OTP")
    }

    suspend fun refreshToken(): Result<AuthTokens> = apiCall {
        val storedRefreshToken = tokenStore.refreshToken.firstOrNull()
            ?: throw ApiException(401, "No refresh token available")
        val response = authApi.refreshToken(RefreshTokenRequest(storedRefreshToken))
        val body = response.requireBody("Token refresh failed")
        tokenStore.saveTokens(body.data.accessToken, body.data.refreshToken)
        body.data
    }

    suspend fun getMe(): Result<User> = apiCall {
        authApi.getMe().requireBody("Failed to load user").data
    }

    suspend fun forgotPassword(email: String): Result<String> = apiCall {
        authApi.forgotPassword(ForgotPasswordRequest(email.trim()))
            .requireBody("Password reset request failed").message
    }

    suspend fun resetPassword(token: String, newPassword: String): Result<String> = apiCall {
        authApi.resetPassword(ResetPasswordRequest(token.trim(), newPassword))
            .requireBody("Password reset failed").message
    }

    suspend fun healthReady(): Result<HealthResponse> = apiCall {
        authApi.healthReady().requireBody("Server is not ready")
    }

    suspend fun logout(): Result<Unit> {
        val remoteResult = apiCall {
            tokenStore.refreshToken.firstOrNull()?.let { refreshToken ->
                authApi.logout(LogoutRequest(refreshToken)).requireBody("Logout failed")
            }
            Unit
        }
        tokenStore.clearTokens()
        return remoteResult
    }

    suspend fun logoutAll(): Result<Unit> {
        val remoteResult = apiCall {
            authApi.logoutAll().requireBody("Logout all failed")
            Unit
        }
        tokenStore.clearTokens()
        return remoteResult
    }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(exception)
        }

    private fun <T> Response<T>.requireBody(fallbackMessage: String): T {
        if (isSuccessful) {
            return body() ?: throw ApiException(code(), "Invalid server response")
        }

        val serverMessage = runCatching {
            errorBody()?.string()?.let { raw ->
                errorJson.decodeFromString<ApiErrorResponse>(raw).message
                    ?: errorJson.decodeFromString<ApiErrorResponse>(raw).error
            }
        }.getOrNull()

        throw ApiException(code(), serverMessage ?: "$fallbackMessage (${code()})")
    }
}
