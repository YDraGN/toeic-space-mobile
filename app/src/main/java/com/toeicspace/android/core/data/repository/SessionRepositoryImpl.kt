package com.toeicspace.android.core.data.repository

import com.toeicspace.android.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class SessionRepositoryImpl
    @Inject
    constructor(
        private val userPreferencesDataSource: UserPreferencesDataSource,
    ) : SessionRepository {
        override val isOnboardingCompleted: Flow<Boolean> =
            userPreferencesDataSource.isOnboardingCompleted

        override suspend fun completeOnboarding() {
            userPreferencesDataSource.setOnboardingCompleted(true)
        }
    }
