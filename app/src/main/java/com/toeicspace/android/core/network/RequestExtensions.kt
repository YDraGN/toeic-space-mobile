package com.toeicspace.android.core.network

import okhttp3.Request
import retrofit2.Invocation

internal val Request.isPublicEndpoint: Boolean
    get() =
        tag(Invocation::class.java)?.method()?.isAnnotationPresent(PublicEndpoint::class.java) ==
            true

internal fun Request.bearerToken(): String? =
    header(AUTHORIZATION_HEADER)?.removePrefix(BEARER_PREFIX)

internal fun Request.withBearer(token: String): Request =
    newBuilder().header(AUTHORIZATION_HEADER, BEARER_PREFIX + token).build()

internal const val AUTHORIZATION_HEADER = "Authorization"
private const val BEARER_PREFIX = "Bearer "
