package com.entrymyslot.app.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ------------------------------------------------------------
// Common API response
// ------------------------------------------------------------

@Serializable
data class ApiMessageResponse(
    val success: Boolean,
    val message: String
)

// ------------------------------------------------------------
// User
// Matches backend UserPublic
// ------------------------------------------------------------

@Serializable
data class User(
    val id: Int,
    val email: String,
    val username: String? = null,

    @SerialName("is_verified")
    val isVerified: Boolean,

    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("created_at")
    val createdAt: String
)

// ------------------------------------------------------------
// Tokens
// Matches backend AuthTokens
// ------------------------------------------------------------

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: String
)

// ------------------------------------------------------------
// Login
// POST /auth/login-enhanced
// ------------------------------------------------------------

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceInfo: String? = null
)

@Serializable
data class LoginData(
    val tokens: AuthTokens,
    val user: User,
    val sessionId: Int
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val data: LoginData
)

// ------------------------------------------------------------
// Register OTP
// POST /auth/register-otp
// ------------------------------------------------------------

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String? = null,
    val password: String
)

@Serializable
data class RegisterOtpResponse(
    val success: Boolean,
    val message: String,
    val expiresInMinutes: Int
)

// ------------------------------------------------------------
// Verify Registration OTP
// POST /auth/verify-registration-otp
// ------------------------------------------------------------

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

@Serializable
data class VerifyOtpData(
    val tokens: AuthTokens,
    val user: User,
    val isNewUser: Boolean
)

@Serializable
data class VerifyOtpResponse(
    val success: Boolean,
    val message: String,
    val data: VerifyOtpData
)

// ------------------------------------------------------------
// Resend Registration OTP
// POST /auth/resend-registration-otp
// ------------------------------------------------------------

@Serializable
data class ResendOtpRequest(
    val email: String
)

@Serializable
data class ResendOtpResponse(
    val success: Boolean,
    val message: String
)

// ------------------------------------------------------------
// Refresh Token
// POST /auth/refresh-token
// ------------------------------------------------------------

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val success: Boolean,
    val data: AuthTokens
)

// ------------------------------------------------------------
// Logout
// POST /auth/logout
// ------------------------------------------------------------

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class LogoutResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

@Serializable
data class GetMeResponse(
    val success: Boolean,
    val data: User
)