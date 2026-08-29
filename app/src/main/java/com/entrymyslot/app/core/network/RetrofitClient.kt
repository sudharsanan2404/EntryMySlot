package com.entrymyslot.app.core.network

import android.content.Context
import com.entrymyslot.app.core.storage.AuthTokenStore
import com.entrymyslot.app.data.auth.AuthApi
import com.entrymyslot.app.data.home.HomeApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import kotlin.jvm.java

object RetrofitClient {

    private const val BASE_URL =
        "http://98.130.20.52:4000/api/v1/"

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
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(authTokenStore)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
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
}
