package com.stewstudio.app.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkMonitor(
    context: Context
) {

    private val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    fun isOnline(): Boolean {
        val network =
            connectivityManager.activeNetwork
                ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(
                network
            ) ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) &&
                capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
    }

    fun observe(): Flow<Boolean> = callbackFlow {

        fun checkConnection() {
            trySend(isOnline())
        }

        val callback =
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(
                    network: Network
                ) {
                    checkConnection()
                }

                override fun onLost(
                    network: Network
                ) {
                    checkConnection()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    checkConnection()
                }
        }

        checkConnection()

        connectivityManager.registerDefaultNetworkCallback(
            callback
        )

        awaitClose {
            connectivityManager.unregisterNetworkCallback(
                callback
            )
        }
    }.distinctUntilChanged()
}