package com.toeicspace.android.core.network.model

import com.toeicspace.android.core.datastore.AccessToken
import com.toeicspace.android.core.datastore.SessionTokens
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

// Backend maximum; used only if the expiry is missing or unreadable.
private val FALLBACK_REFRESH_LIFETIME_MILLIS = TimeUnit.DAYS.toMillis(7)

/**
 * The access token expiry comes from `expiresIn`, measured on this device, so a wrong device clock
 * cannot make the app send expired tokens. Returns null when the backend did not include a refresh
 * token (it was not called as a native client).
 */
fun AuthResponseDto.toSessionTokens(nowMillis: Long): SessionTokens? {
    val refresh = refreshToken ?: return null
    val refreshExpiresAt =
        refreshTokenExpiresAt?.toEpochMillisOrNull()
            ?: (nowMillis + FALLBACK_REFRESH_LIFETIME_MILLIS)
    return SessionTokens(
        accessToken =
            AccessToken(
                value = accessToken,
                expiresAtMillis = nowMillis + TimeUnit.SECONDS.toMillis(expiresIn.toLong()),
            ),
        refreshToken = refresh,
        refreshTokenExpiresAtMillis = refreshExpiresAt,
    )
}

private fun String.toEpochMillisOrNull(): Long? =
    try {
        OffsetDateTime.parse(this).toInstant().toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
