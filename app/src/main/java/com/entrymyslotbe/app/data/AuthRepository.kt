package com.entrymyslotbe.app.data

import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.auth.SessionStore
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
            sessionStore.save(AuthScope.ADMIN, token)
        }
        return result
    }

    suspend fun organizerLogin(email: String, password: String): ApiResult<OrganizerLoginResponse?> {
        val result = gateway.data { organizerLogin(OrganizerLoginRequest(email, password)) }
        if (result is ApiResult.Success) {
            val body = result.value
            val access = body?.accessToken
                ?: return ApiResult.UnexpectedError("Organizer login response did not contain a token.")
            sessionStore.save(AuthScope.ORGANIZER, access, body.refreshToken)
        }
        return result
    }

    suspend fun refreshActiveSession(): ApiResult<*> = when (sessionStore.activeScope()) {
        AuthScope.USER -> {
            val refresh = sessionStore.refreshToken(AuthScope.USER)
                ?: return ApiResult.Unauthorized("No user refresh token is stored.")
            saveUserSession(gateway.data { refreshToken(RefreshTokenRequest(refresh)) }, refreshing = true)
        }
        AuthScope.ORGANIZER -> {
            val refresh = sessionStore.refreshToken(AuthScope.ORGANIZER)
                ?: return ApiResult.Unauthorized("No organizer refresh token is stored.")
            val result = gateway.data { organizerRefresh(RefreshTokenRequest(refresh)) }
            if (result is ApiResult.Success) {
                val body = result.value
                val access = body?.accessToken
                    ?: return ApiResult.UnexpectedError("Refresh response did not contain a token.")
                sessionStore.save(AuthScope.ORGANIZER, access, body.refreshToken ?: refresh)
            }
            result
        }
        AuthScope.ADMIN -> ApiResult.Unauthorized("Admin sessions cannot be refreshed. Please log in again.")
    }

    suspend fun logout(): ApiResult<Unit> {
        val scope = sessionStore.activeScope()
        val refresh = sessionStore.refreshToken(scope)
        return try {
            if (scope == AuthScope.USER && refresh != null) {
                messageOnly { logout(RefreshTokenRequest(refresh)) }
            } else {
                ApiResult.Success(Unit, 200, "Local session cleared.")
            }
        } finally {
            sessionStore.clear(scope)
        }
    }

    suspend fun logoutAllUserDevices(): ApiResult<Unit> {
        sessionStore.select(AuthScope.USER)
        val result = messageOnly { logoutAll() }
        if (result is ApiResult.Success) sessionStore.clear(AuthScope.USER)
        return result
    }

    fun useSession(scope: AuthScope) = sessionStore.select(scope)

    private fun saveUserSession(result: ApiResult<LoginResponse?>, refreshing: Boolean = false): ApiResult<LoginResponse?> {
        if (result is ApiResult.Success) {
            val response = result.value
            val access = response?.resolvedAccessToken()
                ?: return ApiResult.UnexpectedError("Login response did not contain an access token.")
            if (!refreshing || response.user != null) sessionStore.saveUser(response.user)
            sessionStore.save(AuthScope.USER, access, response.resolvedRefreshToken())
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
