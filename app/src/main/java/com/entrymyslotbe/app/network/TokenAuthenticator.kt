package com.entrymyslotbe.app.network

import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.auth.SessionStore
import com.entrymyslotbe.app.auth.SessionSnapshot
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
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            ?.takeIf(String::isNotBlank) ?: return null
        val original = response.request.tag(SessionSnapshot::class.java) ?: return null
        val current = sessionStore.snapshot(original.scope)
        // A request from a previous login must never be retried as a different user.
        if (current.generation != original.generation || current.accessToken.isNullOrBlank()) return null
        if (responseCount(response) >= 2) {
            sessionStore.clearIfCurrent(original)
            return null
        }

        val scope = original.scope
        if (scope == AuthScope.ADMIN) {
            sessionStore.clearIfCurrent(original)
            return null
        }

        val currentToken = current.accessToken
        if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .tag(SessionSnapshot::class.java, current)
                .build()
        }

        val refreshToken = current.refreshToken?.takeIf(String::isNotBlank) ?: run {
            sessionStore.clearIfCurrent(current)
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
                    if (refreshResponse.code == 401 || refreshResponse.code == 403) sessionStore.clearIfCurrent(current)
                    return@use null
                }
                val json = JsonParser.parseString(refreshResponse.body?.string()).asJsonObject
                val data = json.getAsJsonObject("data") ?: return@use null
                val tokens = data.getAsJsonObject("tokens") ?: data
                val newAccess = tokens.get("accessToken")?.takeUnless { it.isJsonNull }?.asString ?: return@use null
                val newRefresh = tokens.get("refreshToken")?.takeUnless { it.isJsonNull }?.asString ?: refreshToken
                if (!sessionStore.refreshIfCurrent(current, newAccess, newRefresh)) return@use null
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .tag(SessionSnapshot::class.java, current.copy(accessToken = newAccess, refreshToken = newRefresh))
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
