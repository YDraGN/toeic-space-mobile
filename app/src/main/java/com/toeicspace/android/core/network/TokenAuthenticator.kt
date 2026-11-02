package com.toeicspace.android.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/** Retries a protected request once with a new access token after a 401. */
class TokenAuthenticator
    @Inject
    constructor(
        private val tokenRefresher: TokenRefresher,
    ) : Authenticator {
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            val request = response.request
            if (request.isPublicEndpoint || response.priorResponse != null) return null
            val staleToken = request.bearerToken() ?: return null

            return when (val result = runBlocking { tokenRefresher.refresh(staleToken) }) {
                is RefreshResult.Refreshed -> request.withBearer(result.accessToken.value)
                else -> null
            }
        }
    }
