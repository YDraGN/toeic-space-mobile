package com.toeicspace.android.testing

import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionRepository(
    isOnboardingCompleted: Boolean = false,
    user: AuthUser? = null,
    isGuest: Boolean = false,
) : SessionRepository {
    private val state =
        MutableStateFlow(
            SessionState.Ready(
                isOnboardingCompleted = isOnboardingCompleted,
                user = user,
                isGuest = isGuest,
            ),
        )

    override val session: Flow<SessionState> = state

    val current: SessionState.Ready get() = state.value

    val currentOnboardingCompleted: Boolean get() = state.value.isOnboardingCompleted

    var signOutCount = 0
        private set

    override suspend fun completeOnboarding() {
        state.value = state.value.copy(isOnboardingCompleted = true)
    }

    override suspend fun continueAsGuest() {
        state.value = state.value.copy(isOnboardingCompleted = true, isGuest = true)
    }

    override suspend fun signOut() {
        signOutCount++
        state.value = state.value.copy(user = null, isGuest = false)
    }

    /** Simulates the session ending on the server, e.g. a failed refresh. */
    fun expireSession() {
        state.value = state.value.copy(user = null, isGuest = false)
    }
}
