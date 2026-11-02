package com.toeicspace.android.core.data.repository

import com.toeicspace.android.core.datastore.SessionTokenStore
import com.toeicspace.android.core.datastore.UserPreferencesDataSource
import com.toeicspace.android.core.di.ApplicationScope
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.network.SessionApiService
import com.toeicspace.android.core.network.model.RefreshTokenRequestDto
import com.toeicspace.android.core.network.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

internal class SessionRepositoryImpl
    @Inject
    constructor(
        private val userPreferencesDataSource: UserPreferencesDataSource,
        private val sessionTokenStore: SessionTokenStore,
        private val sessionApiService: SessionApiService,
        private val json: Json,
        @param:ApplicationScope private val applicationScope: CoroutineScope,
    ) : SessionRepository {
        override val session: Flow<SessionState> =
            combine(
                userPreferencesDataSource.userPreferences,
                sessionTokenStore.user,
            ) { preferences, user ->
                SessionState.Ready(
                    isOnboardingCompleted = preferences.isOnboardingCompleted,
                    user = user,
                    isGuest = user == null && preferences.isGuest,
                ) as SessionState
            }.onStart { emit(SessionState.Loading) }
                .distinctUntilChanged()

        override suspend fun completeOnboarding() {
            userPreferencesDataSource.setOnboardingCompleted(true)
        }

        override suspend fun continueAsGuest() {
            userPreferencesDataSource.setOnboardingCompleted(true)
            userPreferencesDataSource.setGuest(true)
        }

        override suspend fun signOut() {
            val refreshToken = sessionTokenStore.refreshToken()
            sessionTokenStore.clear()
            userPreferencesDataSource.setGuest(false)

            // Best effort: offline, the token simply expires on the server.
            if (refreshToken != null) {
                applicationScope.launch {
                    safeApiCall(
                        json,
                    ) { sessionApiService.logout(RefreshTokenRequestDto(refreshToken)) }
                }
            }
        }
    }
