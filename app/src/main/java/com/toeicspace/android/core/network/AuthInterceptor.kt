package com.toeicspace.android.core.network

import com.toeicspace.android.core.datastore.SessionTokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adds the Bearer token to protected requests. The access token lives only in memory, so after a
 * restart, or shortly before it expires, it is refreshed here first instead of spending a request on
 * a guaranteed 401.
 */
class AuthInterceptor
    @Inject
    constructor(
        private val sessionTokenStore: SessionTokenStore,
        private val tokenRefresher: TokenRefresher,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (request.isPublicEndpoint) return chain.proceed(request)

            val stored = sessionTokenStore.accessToken()
            val token =
                if (tokenRefresher.isUsable(stored)) {
                    stored
                } else {
                    // OkHttp runs interceptors on its own background threads.
                    when (val result = runBlocking { tokenRefresher.refresh(stored?.value) }) {
                        is RefreshResult.Refreshed -> result.accessToken
                        else -> null
                    }
                }

            return chain.proceed(token?.let { request.withBearer(it.value) } ?: request)
        }
    }
