package com.entrymyslot.app

import android.content.Context
import com.entrymyslot.app.core.network.RetrofitClient
import com.entrymyslot.app.core.storage.AuthTokenStore
import com.entrymyslot.app.data.auth.AuthRepository

class AppContainer(
    context: Context
) {

    private val authTokenStore = AuthTokenStore(
        context = context.applicationContext
    )

    val authRepository = AuthRepository(
        authApi = RetrofitClient.authApi,
        tokenStore = authTokenStore
    )
}