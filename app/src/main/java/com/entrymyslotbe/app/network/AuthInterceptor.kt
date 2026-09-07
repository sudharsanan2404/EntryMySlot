package com.entrymyslotbe.app.network

import com.entrymyslotbe.app.auth.SessionStore
import com.entrymyslotbe.app.auth.SessionSnapshot
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStore: SessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.header("Authorization") != null) return chain.proceed(original)

        val session = sessionStore.snapshot()
        val token = session.accessToken
        val authenticated = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .tag(SessionSnapshot::class.java, session)
                .build()
        }
        return chain.proceed(authenticated)
    }
}
