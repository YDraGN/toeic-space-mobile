package com.toeicspace.android.testing

import com.toeicspace.android.core.data.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionRepository(
    isOnboardingCompleted: Boolean = false,
) : SessionRepository {
    private val onboardingCompleted = MutableStateFlow(isOnboardingCompleted)

    override val isOnboardingCompleted: Flow<Boolean> = onboardingCompleted

    val currentOnboardingCompleted: Boolean
        get() = onboardingCompleted.value

    override suspend fun completeOnboarding() {
        onboardingCompleted.value = true
    }
}
