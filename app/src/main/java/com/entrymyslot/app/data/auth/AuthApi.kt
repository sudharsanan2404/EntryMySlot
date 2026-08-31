package com.entrymyslot.app.data.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {

    @GET("/health/ready")
    suspend fun healthReady(): Response<HealthResponse>

    // ------------------------------------------------------------
    // LOGIN
    // POST /api/v1/auth/login-enhanced
    // ------------------------------------------------------------

    @POST("auth/login-enhanced")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>


    // ------------------------------------------------------------
    // REGISTER - REQUEST OTP
    // POST /api/v1/auth/register-otp
    // ------------------------------------------------------------

    @POST("auth/register-otp")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterOtpResponse>


    // ------------------------------------------------------------
    // VERIFY REGISTRATION OTP
    // POST /api/v1/auth/verify-registration-otp
    // ------------------------------------------------------------

    @POST("auth/verify-registration-otp")
    suspend fun verifyRegistrationOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>


    // ------------------------------------------------------------
    // RESEND REGISTRATION OTP
    // POST /api/v1/auth/resend-registration-otp
    // ------------------------------------------------------------

    @POST("auth/resend-registration-otp")
    suspend fun resendRegistrationOtp(
        @Body request: ResendOtpRequest
    ): Response<ResendOtpResponse>


    // ------------------------------------------------------------
    // REFRESH TOKEN
    // POST /api/v1/auth/refresh-token
    // ------------------------------------------------------------

    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>


    // ------------------------------------------------------------
    // LOGOUT
    // POST /api/v1/auth/logout
    // ------------------------------------------------------------

    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<LogoutResponse>


    // ------------------------------------------------------------
    // LOGOUT ALL DEVICES
    // POST /api/v1/auth/logout-all
    // ------------------------------------------------------------

    @POST("auth/logout-all")
    suspend fun logoutAll(): Response<LogoutResponse>


    // ------------------------------------------------------------
    // FORGOT PASSWORD
    // POST /api/v1/auth/forgot-password
    // ------------------------------------------------------------

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ApiMessageResponse>


    // ------------------------------------------------------------
    // RESET PASSWORD
    // POST /api/v1/auth/reset-password
    // ------------------------------------------------------------

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiMessageResponse>


    // ------------------------------------------------------------
    // GET CURRENT USER
    // GET /api/v1/auth/me
    // ------------------------------------------------------------

    @GET("auth/me")
    suspend fun getMe(): Response<GetMeResponse>
}
