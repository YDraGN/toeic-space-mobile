package com.toeicspace.android.core.data.repository

import kotlinx.coroutines.flow.Flow

/** Single source of truth for the app session (onboarding state, later the signed-in user). */
interface SessionRepository {
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()
}
