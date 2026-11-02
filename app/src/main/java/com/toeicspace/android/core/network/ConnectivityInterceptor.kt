package com.toeicspace.android.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/** The device has no network, as opposed to the server not answering. */
class NoConnectivityException : IOException("No network connection")

/**
 * Fails fast when the device is offline, so [safeApiCall] can tell "no network" apart from
 * "server unreachable". Without it both surface as the same [IOException].
 */
class ConnectivityInterceptor
    @Inject
    constructor(
        private val networkMonitor: NetworkMonitor,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!networkMonitor.isCurrentlyOnline()) throw NoConnectivityException()
            return chain.proceed(chain.request())
        }
    }
