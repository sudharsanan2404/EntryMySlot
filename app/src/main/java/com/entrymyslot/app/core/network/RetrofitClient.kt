package com.entrymyslot.app.core.network

import android.content.Context
import com.entrymyslot.app.BuildConfig
import com.entrymyslot.app.core.storage.AuthTokenStore
import com.entrymyslot.app.data.auth.AuthApi
import com.entrymyslot.app.data.auth.RefreshTokenRequest
import com.entrymyslot.app.data.booking.BookingApi
import com.entrymyslot.app.data.home.HomeApi
import com.entrymyslot.app.data.search.SearchApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitClient {

    private lateinit var authTokenStore: AuthTokenStore
    private lateinit var retrofit: Retrofit

    fun initialize(context: Context) {

        authTokenStore = AuthTokenStore(context.applicationContext)

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val authInterceptor = AuthInterceptor(authTokenStore)

        val refreshClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val refreshApi = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(refreshClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(SessionAuthenticator(authTokenStore, refreshApi))
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
    }

    val authApi: AuthApi
        get() = retrofit.create(AuthApi::class.java)

    val homeApi: HomeApi
        get() = retrofit.create(HomeApi::class.java)

    val searchApi: SearchApi
        get() = retrofit.create(SearchApi::class.java)

    val bookingApi: BookingApi
        get() = retrofit.create(BookingApi::class.java)

    private class SessionAuthenticator(
        private val tokenStore: AuthTokenStore,
        private val refreshApi: AuthApi
    ) : Authenticator {

        private val refreshLock = Any()

        override fun authenticate(route: Route?, response: Response): Request? {
            if (responseCount(response) >= 2) return null

            return synchronized(refreshLock) {
                val requestToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")
                val currentAccessToken = runBlocking { tokenStore.accessToken.firstOrNull() }

                if (!currentAccessToken.isNullOrBlank() && currentAccessToken != requestToken) {
                    return@synchronized response.request.withBearer(currentAccessToken)
                }

                val refreshToken = runBlocking { tokenStore.refreshToken.firstOrNull() }
                    ?: return@synchronized null

                val refreshResponse = runCatching {
                    runBlocking {
                        refreshApi.refreshToken(RefreshTokenRequest(refreshToken))
                    }
                }.getOrNull() ?: return@synchronized null

                val newTokens = refreshResponse.body()?.data
                if (refreshResponse.isSuccessful && newTokens != null) {
                    runBlocking {
                        tokenStore.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                    }
                    response.request.withBearer(newTokens.accessToken)
                } else {
                    if (refreshResponse.code() == 400 || refreshResponse.code() == 401) {
                        runBlocking { tokenStore.clearTokens() }
                    }
                    null
                }
            }
        }

        private fun Request.withBearer(token: String): Request =
            newBuilder().header("Authorization", "Bearer $token").build()

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }
    }

}
