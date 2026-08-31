package com.entrymyslot.app.core.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.authDataStore by preferencesDataStore(
    name = "auth_preferences"
)

class AuthTokenStore(
    private val context: Context
) {

    private companion object {
        const val KEY_ALIAS = "entrymyslot_auth_tokens"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ENCRYPTED_PREFIX = "v1:"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val accessToken: Flow<String?> =
        context.authDataStore.data.map { preferences ->
            preferences[Keys.ACCESS_TOKEN]?.let(::decryptOrReadLegacy)
        }

    val refreshToken: Flow<String?> =
        context.authDataStore.data.map { preferences ->
            preferences[Keys.REFRESH_TOKEN]?.let(::decryptOrReadLegacy)
        }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        context.authDataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = encrypt(accessToken)
            preferences[Keys.REFRESH_TOKEN] = encrypt(refreshToken)
        }
    }

    suspend fun clearTokens() {
        context.authDataStore.edit { preferences ->
            preferences.remove(Keys.ACCESS_TOKEN)
            preferences.remove(Keys.REFRESH_TOKEN)
        }
    }

    suspend fun migrateLegacyTokens() {
        val current = context.authDataStore.data.first()
        val access = current[Keys.ACCESS_TOKEN]
        val refresh = current[Keys.REFRESH_TOKEN]
        if (access?.startsWith(ENCRYPTED_PREFIX) != false &&
            refresh?.startsWith(ENCRYPTED_PREFIX) != false
        ) return

        context.authDataStore.edit { preferences ->
            access?.takeUnless { it.startsWith(ENCRYPTED_PREFIX) }?.let {
                preferences[Keys.ACCESS_TOKEN] = encrypt(it)
            }
            refresh?.takeUnless { it.startsWith(ENCRYPTED_PREFIX) }?.let {
                preferences[Keys.REFRESH_TOKEN] = encrypt(it)
            }
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encrypted = Base64.encodeToString(
            cipher.doFinal(value.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )

        return "$ENCRYPTED_PREFIX$iv:$encrypted"
    }

    private fun decryptOrReadLegacy(value: String): String? {
        if (!value.startsWith(ENCRYPTED_PREFIX)) {
            return value
        }

        return runCatching {
            val parts = value.removePrefix(ENCRYPTED_PREFIX).split(':', limit = 2)
            require(parts.size == 2)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP))
            )

            cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP))
                .toString(Charsets.UTF_8)
        }.getOrNull()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true)
                        .build()
                )
            }
            .generateKey()
    }
}
