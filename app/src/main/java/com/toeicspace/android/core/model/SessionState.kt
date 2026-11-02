package com.toeicspace.android.core.model

sealed interface SessionState {
    data object Loading : SessionState

    /**
     * Read from local storage only, so it is available offline.
     * [user] is null for guests and signed-out users; [isGuest] tells them apart.
     */
    data class Ready(
        val isOnboardingCompleted: Boolean,
        val user: AuthUser?,
        val isGuest: Boolean,
    ) : SessionState {
        val isSignedIn: Boolean get() = user != null

        /** Guests and signed-in users may use the main app; everyone else is sent to sign in. */
        val canEnterApp: Boolean get() = isSignedIn || isGuest
    }
}
