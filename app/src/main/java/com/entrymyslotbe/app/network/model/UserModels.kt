package com.entrymyslotbe.app.network.model

import com.google.gson.annotations.SerializedName

// ========== USER ==========
data class User(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val username: String? = null,
    val phone: String? = null,
    @SerializedName(value = "isVerified", alternate = ["is_verified"])
    val isVerified: Boolean = false,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null
)

data class AuthTokens(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: String? = null,
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String? = null
)

data class RegistrationStart(
    val email: String? = null,
    val expiresInMinutes: Int? = null
)

data class VerifyRegistrationOtpRequest(
    val email: String,
    val otp: String
)

data class EmailRequest(val email: String)

data class DeviceLoginRequest(
    val email: String,
    val password: String,
    val deviceInfo: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class LoginResponse(
    val user: User? = null,
    val tokens: AuthTokens? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val sessionId: String? = null,
    val isNewUser: Boolean? = null,
) {
    fun resolvedAccessToken(): String? = accessToken ?: tokens?.accessToken
    fun resolvedRefreshToken(): String? = refreshToken ?: tokens?.refreshToken
}

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class UpdateProfileRequest(
    val username: String
)

data class Session(
    val id: String? = null,
    val device: String? = null,
    val ip: String? = null,
    val createdAt: String? = null,
    val lastActive: String? = null
)

data class RevokeSessionRequest(val sessionId: String)

data class OTPResponse(
    val message: String? = null
)
