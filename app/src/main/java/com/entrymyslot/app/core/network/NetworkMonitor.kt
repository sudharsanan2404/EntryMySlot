package com.entrymyslot.app.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkMonitor(context: Context) {
    private val connectivityManager = context.applicationContext
        .getSystemService(ConnectivityManager::class.java)

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(network.isValidated())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(networkCapabilities.hasValidatedInternet())
            }

            override fun onLost(network: Network) {
                trySend(isCurrentlyOnline())
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        trySend(isCurrentlyOnline())
        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()

    fun isCurrentlyOnline(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        return activeNetwork.isValidated()
    }

    private fun Network.isValidated(): Boolean =
        connectivityManager.getNetworkCapabilities(this)?.hasValidatedInternet() == true

    private fun NetworkCapabilities.hasValidatedInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
