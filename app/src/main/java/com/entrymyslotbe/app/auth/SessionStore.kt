package com.entrymyslotbe.app.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.entrymyslotbe.app.network.model.User
import com.google.gson.Gson

enum class AuthScope(val wireName: String) {
    USER("user"),
    ADMIN("admin"),
    ORGANIZER("organizer"),
}

data class SessionState(
    val activeScope: AuthScope = AuthScope.USER,
    val authenticatedScopes: Set<AuthScope> = emptySet(),
)

class SessionStore(context: Context) {
    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREF_FILE,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _state = MutableStateFlow(readState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    @Synchronized
    fun accessToken(scope: AuthScope = activeScope()): String? =
        preferences.getString(accessKey(scope), null)

    @Synchronized
    fun refreshToken(scope: AuthScope = activeScope()): String? =
        preferences.getString(refreshKey(scope), null)

    @Synchronized
    fun user(): User? = runCatching {
        preferences.getString("user_identity", null)?.let { Gson().fromJson(it, User::class.java) }
    }.getOrNull()

    @Synchronized
    fun saveUser(user: User?) {
        preferences.edit().putString("user_identity", user?.let { Gson().toJson(it) }).apply()
    }

    @Synchronized
    fun activeScope(): AuthScope = runCatching {
        AuthScope.valueOf(preferences.getString(KEY_ACTIVE_SCOPE, AuthScope.USER.name)!!)
    }.getOrDefault(AuthScope.USER)

    @Synchronized
    fun select(scope: AuthScope) {
        preferences.edit().putString(KEY_ACTIVE_SCOPE, scope.name).apply()
        publishState()
    }

    @Synchronized
    fun save(scope: AuthScope, accessToken: String, refreshToken: String? = null) {
        preferences.edit()
            .putString(accessKey(scope), accessToken)
            .apply {
                if (refreshToken != null) putString(refreshKey(scope), refreshToken)
            }
            .putString(KEY_ACTIVE_SCOPE, scope.name)
            .commit()
        publishState()
    }

    @Synchronized
    fun clear(scope: AuthScope) {
        // Complete the disk write before logout/session-expiry navigation can stop the process.
        preferences.edit().remove(accessKey(scope)).remove(refreshKey(scope))
            .apply { if (scope == AuthScope.USER) remove("user_identity") }
            .commit()
        publishState()
    }

    @Synchronized
    fun clearAll() {
        preferences.edit().clear().commit()
        publishState()
    }

    private fun readState(): SessionState = SessionState(
        activeScope = activeScope(),
        authenticatedScopes = AuthScope.entries.filterTo(mutableSetOf()) {
            !preferences.getString(accessKey(it), null).isNullOrBlank()
        },
    )

    private fun publishState() {
        _state.value = readState()
    }

    private fun accessKey(scope: AuthScope) = "${scope.wireName}_access_token"
    private fun refreshKey(scope: AuthScope) = "${scope.wireName}_refresh_token"

    private companion object {
        const val PREF_FILE = "entry_my_slot_secure_session"
        const val KEY_ACTIVE_SCOPE = "active_scope"
    }
}
