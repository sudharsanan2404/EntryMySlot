package com.entrymyslotbe.app.network

import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.auth.SessionStore
import com.google.gson.JsonParser
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionStore: SessionStore,
    private val baseUrl: String,
) : Authenticator {
    private val refreshClient = OkHttpClient.Builder().build()

    override fun authenticate(route: Route?, response: Response): Request? = synchronized(this) {
        if (isAuthEndpoint(response.request)) return null
        if (responseCount(response) >= 2) {
            sessionStore.clear(sessionStore.activeScope())
            return null
        }

        val scope = sessionStore.activeScope()
        if (scope == AuthScope.ADMIN) {
            sessionStore.clear(scope)
            return null
        }

        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        val currentToken = sessionStore.accessToken(scope)
        if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        val refreshToken = sessionStore.refreshToken(scope) ?: run {
            sessionStore.clear(scope)
            return null
        }
        val endpoint = when (scope) {
            AuthScope.USER -> "auth/refresh-token"
            AuthScope.ORGANIZER -> "organizer/auth/refresh"
            AuthScope.ADMIN -> return null
        }
        val escaped = refreshToken.replace("\\", "\\\\").replace("\"", "\\\"")
        val body = "{\"refreshToken\":\"$escaped\"}"
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val refreshRequest = Request.Builder()
            .url(baseUrl + endpoint)
            .post(body)
            .build()

        runCatching {
            refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) {
                    if (refreshResponse.code in setOf(401, 403)) sessionStore.clear(scope)
                    return@use null
                }
                val json = JsonParser.parseString(refreshResponse.body?.string()).asJsonObject
                val data = json.getAsJsonObject("data") ?: return@use null
                val tokens = data.getAsJsonObject("tokens") ?: data
                val newAccess = tokens.get("accessToken")?.takeUnless { it.isJsonNull }?.asString ?: return@use null
                val newRefresh = tokens.get("refreshToken")?.takeUnless { it.isJsonNull }?.asString ?: refreshToken
                sessionStore.save(scope, newAccess, newRefresh)
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
            }
        }.getOrNull()
    }

    private fun isAuthEndpoint(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.endsWith("/auth/login") ||
            path.endsWith("/auth/refresh-token") ||
            path.endsWith("/admin/login") ||
            path.endsWith("/organizer/auth/login") ||
            path.endsWith("/organizer/auth/refresh")
    }

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
