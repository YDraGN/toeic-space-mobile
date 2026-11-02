package com.toeicspace.android.core.network

import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.datastore.AccessToken
import com.toeicspace.android.core.datastore.SessionTokenStore
import com.toeicspace.android.core.network.model.RefreshTokenRequestDto
import com.toeicspace.android.core.network.model.toAuthUser
import com.toeicspace.android.core.network.model.toSessionTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// Refresh a little early so a token never expires on its way to the server.
private val EXPIRY_MARGIN_MILLIS = TimeUnit.SECONDS.toMillis(30)

sealed interface RefreshResult {
    data class Refreshed(
        val accessToken: AccessToken,
    ) : RefreshResult

    /** No session, or the backend rejected it. The local session has been cleared. */
    data object SessionEnded : RefreshResult

    /** Could not reach the backend. The session is kept so the user stays signed in offline. */
    data object Unavailable : RefreshResult
}

/**
 * Refresh tokens are single use and reusing one revokes every session of the account, so only one
 * refresh may run at a time. Callers that waited on the lock reuse the token the first one obtained.
 */
@Singleton
class TokenRefresher
    @Inject
    constructor(
        private val sessionApiService: SessionApiService,
        private val sessionTokenStore: SessionTokenStore,
        private val clock: Clock,
    ) {
        private val mutex = Mutex()

        fun isUsable(token: AccessToken?): Boolean =
            token != null && token.expiresAtMillis - EXPIRY_MARGIN_MILLIS > clock.nowMillis()

        /** Returns a usable access token, refreshing unless one newer than [staleToken] is already held. */
        suspend fun refresh(staleToken: String?): RefreshResult =
            mutex.withLock {
                val current = sessionTokenStore.accessToken()
                if (current != null && current.value != staleToken && isUsable(current)) {
                    return@withLock RefreshResult.Refreshed(current)
                }

                // Missing or already expired on this device: no point asking the server.
                val refreshToken =
                    sessionTokenStore.refreshToken() ?: return@withLock endSession()

                try {
                    val response = sessionApiService.refresh(RefreshTokenRequestDto(refreshToken))
                    val tokens =
                        response.toSessionTokens(clock.nowMillis()) ?: return@withLock endSession()
                    sessionTokenStore.save(tokens, response.user.toAuthUser())
                    RefreshResult.Refreshed(tokens.accessToken)
                } catch (exception: HttpException) {
                    // 401 means the refresh token is invalid, expired or revoked. Anything else is a
                    // server problem, so the session is kept and the next request tries again.
                    if (exception.code() == 401) endSession() else RefreshResult.Unavailable
                } catch (_: IOException) {
                    RefreshResult.Unavailable
                }
            }

        private suspend fun endSession(): RefreshResult {
            sessionTokenStore.clear()
            return RefreshResult.SessionEnded
        }
    }
