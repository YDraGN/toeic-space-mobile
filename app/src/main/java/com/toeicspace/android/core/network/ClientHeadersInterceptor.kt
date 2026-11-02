package com.toeicspace.android.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Identifies the app as a native client, so Identity returns refresh and reset tokens in the body
 * instead of browser cookies. The CSRF header is still required on refresh, logout and password reset;
 * sending it everywhere keeps the call sites simple.
 */
class ClientHeadersInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response =
            chain.proceed(
                chain
                    .request()
                    .newBuilder()
                    .header(CLIENT_TYPE_HEADER, CLIENT_TYPE_MOBILE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .build(),
            )

        private companion object {
            const val CLIENT_TYPE_HEADER = "X-Client-Type"
            const val CLIENT_TYPE_MOBILE = "mobile"
            const val CSRF_HEADER = "X-CSRF-Protection"
            const val CSRF_VALUE = "1"
        }
    }
