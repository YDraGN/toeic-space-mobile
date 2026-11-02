package com.toeicspace.android.core.data.repository

import com.toeicspace.android.core.model.SessionState
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the app session. Built from local storage only, so the app opens
 * straight into the right screen even when offline.
 */
interface SessionRepository {
    val session: Flow<SessionState>

    suspend fun completeOnboarding()

    /** Lets the user into the app without an account. Nothing is sent to the backend. */
    suspend fun continueAsGuest()

    /**
     * Ends the session on this device at once (works offline), then revokes the refresh token on the
     * server in the background when it is reachable.
     */
    suspend fun signOut()
}
