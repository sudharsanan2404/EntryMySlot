package com.entrymyslot.app.data.auth

import com.entrymyslot.app.core.storage.AuthTokenStore
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStore: AuthTokenStore
) {

    // ------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------

    suspend fun login(
        email: String,
        password: String
    ): Result<LoginData> {

        return try {
            val response = authApi.login(
                LoginRequest(
                    email = email.trim(),
                    password = password
                )
            )

            if (response.isSuccessful) {

                val body = response.body()

                if (body?.success == true) {

                    tokenStore.saveTokens(
                        accessToken = body.data.tokens.accessToken,
                        refreshToken = body.data.tokens.refreshToken
                    )

                    Result.success(body.data)

                } else {
                    Result.failure(
                        Exception("Login failed")
                    )
                }

            } else {

                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Login failed (${response.code()})"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ------------------------------------------------------------
    // REGISTER - REQUEST OTP
    // ------------------------------------------------------------

    suspend fun register(
        email: String,
        username: String?,
        password: String
    ): Result<RegisterOtpResponse> {

        return try {

            val response = authApi.register(
                RegisterRequest(
                    email = email.trim(),
                    username = username?.trim(),
                    password = password
                )
            )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(
                        Exception("Invalid server response")
                    )
                }

            } else {

                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Registration failed (${response.code()})"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ------------------------------------------------------------
    // VERIFY OTP
    // ------------------------------------------------------------

    suspend fun verifyRegistrationOtp(
        email: String,
        otp: String,
        deviceInfo: String? = null
    ): Result<VerifyOtpData> {

        return try {

            val response = authApi.verifyRegistrationOtp(
                VerifyOtpRequest(
                    email = email.trim(),
                    otp = otp.trim()
                )
            )

            if (response.isSuccessful) {

                val body = response.body()

                if (body?.success == true) {

                    tokenStore.saveTokens(
                        accessToken = body.data.tokens.accessToken,
                        refreshToken = body.data.tokens.refreshToken
                    )

                    Result.success(body.data)

                } else {

                    Result.failure(
                        Exception(
                            body?.message ?: "OTP verification failed"
                        )
                    )
                }

            } else {

                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "OTP verification failed (${response.code()})"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ------------------------------------------------------------
    // RESEND OTP
    // ------------------------------------------------------------

    suspend fun resendRegistrationOtp(
        email: String
    ): Result<ResendOtpResponse> {

        return try {

            val response = authApi.resendRegistrationOtp(
                ResendOtpRequest(
                    email = email.trim()
                )
            )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(
                        Exception("Invalid server response")
                    )
                }

            } else {

                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to resend OTP (${response.code()})"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ------------------------------------------------------------
    // REFRESH TOKEN
    // ------------------------------------------------------------

    suspend fun refreshToken(): Result<AuthTokens> {

        return try {

            val refreshToken = tokenStore.refreshToken.firstOrNull()

            if (refreshToken.isNullOrBlank()) {
                return Result.failure(
                    Exception("No refresh token available")
                )
            }

            val response = authApi.refreshToken(
                RefreshTokenRequest(
                    refreshToken = refreshToken
                )
            )

            if (response.isSuccessful) {

                val body = response.body()

                if (body?.success == true) {

                    val newTokens = body.data

                    tokenStore.saveTokens(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken
                    )

                    Result.success(newTokens)

                } else {

                    Result.failure(
                        Exception("Token refresh failed")
                    )
                }

            } else {

                Result.failure(
                    Exception("Token refresh failed (${response.code()})")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ------------------------------------------------------------
    // GET CURRENT USER
    // ------------------------------------------------------------

    suspend fun getMe(): Result<User> {

        return try {

            val response = authApi.getMe()

            if (response.isSuccessful) {

                val body = response.body()

                if (body?.success == true) {
                    Result.success(body.data)
                } else {
                    Result.failure(
                        Exception("Failed to load user")
                    )
                }

            } else {

                Result.failure(
                    Exception("Failed to load user (${response.code()})")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ------------------------------------------------------------
    // LOGOUT
    // ------------------------------------------------------------

    suspend fun logout(): Result<Unit> {

        return try {

            val refreshToken = tokenStore.refreshToken.firstOrNull()

            if (!refreshToken.isNullOrBlank()) {

                authApi.logout(
                    LogoutRequest(
                        refreshToken = refreshToken
                    )
                )
            }

            tokenStore.clearTokens()

            Result.success(Unit)

        } catch (e: Exception) {

            // Local logout should still happen
            tokenStore.clearTokens()

            Result.failure(e)
        }
    }


    // ------------------------------------------------------------
    // LOGOUT ALL
    // ------------------------------------------------------------

    suspend fun logoutAll(): Result<Unit> {

        return try {

            val response = authApi.logoutAll()

            tokenStore.clearTokens()

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Logout all failed (${response.code()})"
                    )
                )
            }

        } catch (e: Exception) {

            tokenStore.clearTokens()

            Result.failure(e)
        }
    }
}