package com.toeicspace.android.core.network

/**
 * Marks a Retrofit method that must be called without an access token (sign in, register, OTP...).
 * The auth interceptor and authenticator skip these requests, so they never trigger a refresh.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicEndpoint
