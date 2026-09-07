package com.entrymyslotbe.app.network

import com.entrymyslotbe.app.auth.SessionStore
import com.entrymyslot.app.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    sessionStore: SessionStore,
    val gson: Gson = GsonBuilder().create(),
) {
    private val httpLogger = HttpLoggingInterceptor().apply {
        // BODY logging would leak login/refresh tokens into Logcat.
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
        redactHeader("Cookie")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(NetworkConstants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(NetworkConstants.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(sessionStore))
        .addInterceptor(httpLogger)
        .authenticator(TokenAuthenticator(sessionStore, NetworkConstants.BASE_URL))
        .build()

    val service: ApiService = Retrofit.Builder()
        .baseUrl(NetworkConstants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ApiService::class.java)
}
