package com.entrymyslot.app.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(
    name = "auth_preferences"
)

class AuthTokenStore(
    private val context: Context
) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val accessToken: Flow<String?> =
        context.authDataStore.data.map { preferences ->
            preferences[Keys.ACCESS_TOKEN]
        }

    val refreshToken: Flow<String?> =
        context.authDataStore.data.map { preferences ->
            preferences[Keys.REFRESH_TOKEN]
        }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        context.authDataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearTokens() {
        context.authDataStore.edit { preferences ->
            preferences.remove(Keys.ACCESS_TOKEN)
            preferences.remove(Keys.REFRESH_TOKEN)
        }
    }
}