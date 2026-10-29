package com.toeicspace.android.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

internal class ConnectivityManagerNetworkMonitor
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : NetworkMonitor {
        override val isOnline: Flow<Boolean> =
            callbackFlow {
                val connectivityManager = context.getSystemService<ConnectivityManager>()
                if (connectivityManager == null) {
                    trySend(false)
                    close()
                    return@callbackFlow
                }

                val callback =
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities,
                        ) {
                            trySend(networkCapabilities.hasInternet())
                        }

                        override fun onLost(network: Network) {
                            trySend(false)
                        }
                    }

                // The callback only fires on changes, so emit the current state first.
                trySend(connectivityManager.isCurrentlyOnline())
                connectivityManager.registerDefaultNetworkCallback(callback)

                awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
            }.distinctUntilChanged()
                .conflate()

        private fun ConnectivityManager.isCurrentlyOnline(): Boolean =
            getNetworkCapabilities(activeNetwork)?.hasInternet() ?: false

        private fun NetworkCapabilities.hasInternet(): Boolean =
            hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
