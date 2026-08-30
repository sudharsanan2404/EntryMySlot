package com.entrymyslot.app.core.network

import com.entrymyslot.app.core.storage.AuthTokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: AuthTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking {
            tokenStore.accessToken.first()
        }

        val request = chain.request().newBuilder()

        if (!accessToken.isNullOrBlank()) {
            request.addHeader(
                "Authorization",
                "Bearer $accessToken"
            )
        }

        return chain.proceed(request.build())
    }
}