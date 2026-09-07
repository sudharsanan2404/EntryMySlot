package com.entrymyslotbe.app.data

import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.auth.SessionStore
import com.entrymyslotbe.app.auth.SessionSnapshot
import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.network.ApiService
import com.entrymyslotbe.app.network.BackendGateway
import com.entrymyslotbe.app.network.model.AdminLoginRequest
import com.entrymyslotbe.app.network.model.AdminLoginResponse
import com.entrymyslotbe.app.network.model.ApiResponse
import com.entrymyslotbe.app.network.model.DeviceLoginRequest
import com.entrymyslotbe.app.network.model.EmailRequest
import com.entrymyslotbe.app.network.model.LoginRequest
import com.entrymyslotbe.app.network.model.LoginResponse
import com.entrymyslotbe.app.network.model.OrganizerLoginRequest
import com.entrymyslotbe.app.network.model.OrganizerLoginResponse
import com.entrymyslotbe.app.network.model.RefreshTokenRequest
import com.entrymyslotbe.app.network.model.RegisterRequest
import com.entrymyslotbe.app.network.model.RegistrationStart
import com.entrymyslotbe.app.network.model.VerifyRegistrationOtpRequest
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class AuthRepository(
    private val gateway: BackendGateway,
    private val sessionStore: SessionStore,
) {
    val sessionState = sessionStore.state

    suspend fun register(
        email: String,
        password: String,
        username: String? = null,
    ): ApiResult<RegistrationStart?> = when (
        val result = gateway.execute { registerEnhanced(RegisterRequest(email, password, username)) }
    ) {
        is ApiResult.Success -> ApiResult.Success(
            RegistrationStart(email, result.value.expiresInMinutes),
            result.httpCode,
            result.value.message,
        )
        is ApiResult.Failure -> result
    }

    suspend fun verifyRegistrationOtp(email: String, otp: String): ApiResult<LoginResponse?> =
        saveUserSession(gateway.data {
            verifyRegistrationOtp(VerifyRegistrationOtpRequest(email, otp))
        })

    suspend fun resendRegistrationOtp(email: String): ApiResult<Unit> =
        messageOnly { resendRegistrationOtp(EmailRequest(email)) }

    suspend fun forgotPassword(email: String): ApiResult<Unit> =
        messageOnly { forgotPassword(com.entrymyslotbe.app.network.model.ForgotPasswordRequest(email)) }

    suspend fun resetPassword(token: String, password: String): ApiResult<Unit> =
        messageOnly { resetPassword(com.entrymyslotbe.app.network.model.ResetPasswordRequest(token, password)) }

    suspend fun login(
        email: String,
        password: String,
        deviceInfo: String? = null,
    ): ApiResult<LoginResponse?> = saveUserSession(
        if (deviceInfo == null) gateway.data { login(LoginRequest(email, password)) }
        else gateway.data { loginEnhanced(DeviceLoginRequest(email, password, deviceInfo)) },
    )

    suspend fun adminLogin(email: String, password: String): ApiResult<AdminLoginResponse?> {
        val result = gateway.data { adminLogin(AdminLoginRequest(email, password)) }
        if (result is ApiResult.Success) {
            val token = result.value?.resolvedAccessToken()
                ?: return ApiResult.UnexpectedError("Admin login response did not contain a token.")
            withContext(Dispatchers.IO) { sessionStore.save(AuthScope.ADMIN, token) }
        }
        return result
    }

    suspend fun organizerLogin(email: String, password: String): ApiResult<OrganizerLoginResponse?> {
        val result = gateway.data { organizerLogin(OrganizerLoginRequest(email, password)) }
        if (result is ApiResult.Success) {
            val body = result.value
            val access = body?.accessToken
                ?: return ApiResult.UnexpectedError("Organizer login response did not contain a token.")
            withContext(Dispatchers.IO) { sessionStore.save(AuthScope.ORGANIZER, access, body.refreshToken) }
        }
        return result
    }

    suspend fun refreshActiveSession(): ApiResult<*> = when (sessionStore.activeScope()) {
        AuthScope.USER -> {
            val expected = sessionStore.snapshot(AuthScope.USER)
            val refresh = expected.refreshToken
                ?: return ApiResult.Unauthorized("No user refresh token is stored.")
            saveUserSession(gateway.data { refreshToken(RefreshTokenRequest(refresh)) }, expectedSession = expected)
        }
        AuthScope.ORGANIZER -> {
            val expected = sessionStore.snapshot(AuthScope.ORGANIZER)
            val refresh = expected.refreshToken
                ?: return ApiResult.Unauthorized("No organizer refresh token is stored.")
            val result = gateway.data { organizerRefresh(RefreshTokenRequest(refresh)) }
            if (result is ApiResult.Success) {
                val body = result.value
                val access = body?.accessToken
                    ?: return ApiResult.UnexpectedError("Refresh response did not contain a token.")
                val saved = withContext(Dispatchers.IO) {
                    sessionStore.refreshIfCurrent(expected, access, body.refreshToken ?: refresh)
                }
                if (!saved) return ApiResult.Unauthorized("The session changed while refreshing. Please try again.")
            }
            result
        }
        AuthScope.ADMIN -> ApiResult.Unauthorized("Admin sessions cannot be refreshed. Please log in again.")
    }

    suspend fun logout(): ApiResult<Unit> {
        val expected = sessionStore.snapshot()
        val scope = expected.scope
        val refresh = expected.refreshToken
        return try {
            if (scope == AuthScope.USER && refresh != null) {
                messageOnly { logout(RefreshTokenRequest(refresh)) }
            } else {
                ApiResult.Success(Unit, 200, "Local session cleared.")
            }
        } finally {
            // Keep logout durable even when leaving the screen cancels its scope,
            // without blocking Compose on encrypted preference disk writes.
            withContext(NonCancellable + Dispatchers.IO) { sessionStore.clearLoginIfCurrent(expected) }
        }
    }

    suspend fun logoutAllUserDevices(): ApiResult<Unit> {
        sessionStore.select(AuthScope.USER)
        val expected = sessionStore.snapshot(AuthScope.USER)
        val result = messageOnly { logoutAll() }
        if (result is ApiResult.Success) withContext(NonCancellable + Dispatchers.IO) {
            sessionStore.clearLoginIfCurrent(expected)
        }
        return result
    }

    fun useSession(scope: AuthScope) = sessionStore.select(scope)

    private suspend fun saveUserSession(result: ApiResult<LoginResponse?>, expectedSession: SessionSnapshot? = null): ApiResult<LoginResponse?> {
        if (result is ApiResult.Success) {
            val response = result.value
            val access = response?.resolvedAccessToken()
                ?: return ApiResult.UnexpectedError("Login response did not contain an access token.")
            val saved = withContext(Dispatchers.IO) {
                if (expectedSession != null) {
                    sessionStore.refreshIfCurrent(expectedSession, access,
                        response.resolvedRefreshToken() ?: expectedSession.refreshToken, response.user)
                } else {
                    sessionStore.saveUserLogin(access, response.resolvedRefreshToken(), response.user)
                    true
                }
            }
            if (!saved) return ApiResult.Unauthorized("The session changed while refreshing. Please try again.")
        }
        return result
    }

    private suspend fun messageOnly(
        call: suspend ApiService.() -> Response<ApiResponse<Any>>,
    ): ApiResult<Unit> = when (val result = gateway.data(call)) {
        is ApiResult.Success -> ApiResult.Success(Unit, result.httpCode, result.message)
        is ApiResult.Failure -> result
    }
}
