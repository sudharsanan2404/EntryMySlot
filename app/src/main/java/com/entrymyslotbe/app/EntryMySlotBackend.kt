package com.entrymyslotbe.app

import android.content.Context
import com.entrymyslotbe.app.auth.SessionStore
import com.entrymyslotbe.app.data.AuthRepository
import com.entrymyslotbe.app.data.HomeRepository
import com.entrymyslotbe.app.network.ApiClient
import com.entrymyslotbe.app.network.ApiService
import com.entrymyslotbe.app.network.BackendGateway
import com.entrymyslotbe.app.network.ConnectivityMonitor

/**
 * Single entry point for the UI project.
 *
 * Typed endpoint functions remain on [api]. UI code should normally call them via
 * [gateway.execute] or [gateway.data] so offline, timeout, HTTP and auth errors have
 * one consistent shape. Authentication flows are wrapped by [auth] because they
 * also persist or clear tokens. Composite home loading is available through [home].
 */
class EntryMySlotBackend private constructor(context: Context) {
    val connectivity = ConnectivityMonitor(context.applicationContext)
    val sessions = SessionStore(context.applicationContext)
    private val client = ApiClient(sessions)

    val api: ApiService = client.service
    val gateway = BackendGateway(api, connectivity, client.gson)
    val auth = AuthRepository(gateway, sessions)
    val home = HomeRepository(gateway)

    companion object {
        @Volatile
        private var instance: EntryMySlotBackend? = null

        fun get(context: Context): EntryMySlotBackend = instance ?: synchronized(this) {
            instance ?: EntryMySlotBackend(context.applicationContext).also { instance = it }
        }
    }
}
