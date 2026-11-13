package com.toeicspace.android.feature.profile.data.repository

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.datastore.SessionTokenStore
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.profile.data.remote.ProfileApiService
import com.toeicspace.android.feature.profile.data.remote.UserProfileDto
import com.toeicspace.android.feature.profile.data.remote.toDomain
import com.toeicspace.android.feature.profile.data.remote.toUpdateRequest
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** A fetched profile and the account it belongs to, so it never shows after switching accounts. */
private data class CachedProfile(
    val userId: String,
    val profile: UserProfile,
)

/**
 * Profile backed by `GET/PUT /users/me`, kept in memory. Before the first fetch (or offline) the
 * session's cached name and email stand in, so the screens open at once.
 */
@Singleton
internal class ProfileRepositoryImpl
    @Inject
    constructor(
        private val profileApiService: ProfileApiService,
        private val sessionRepository: SessionRepository,
        private val sessionTokenStore: SessionTokenStore,
        private val json: Json,
    ) : ProfileRepository {
        private val cache = MutableStateFlow<CachedProfile?>(null)

        // Wait for the stored session: an early "still loading" would read as a guest and close
        // the account screen right after it opens.
        private val signedInUser: Flow<AuthUser?> =
            sessionRepository.session
                .filterIsInstance<SessionState.Ready>()
                .map { it.user }
                .distinctUntilChanged()

        override val profile: Flow<UserProfile?> =
            combine(signedInUser, cache) { user, cached ->
                when {
                    user == null -> null
                    cached?.userId == user.id -> cached.profile
                    else -> UserProfile(fullName = user.fullName, email = user.email)
                }
            }.distinctUntilChanged()

        override suspend fun refresh(): AppResult<Unit> {
            val user = signedInUser.first() ?: return AppResult.Failure(AppError.Unauthorized)
            return safeApiCall(json) { profileApiService.getMyProfile() }
                .map { dto -> store(user, dto) }
                .map { }
        }

        override suspend fun updateProfile(profile: UserProfile): AppResult<UserProfile> {
            val user = signedInUser.first() ?: return AppResult.Failure(AppError.Unauthorized)
            val request = profile.toUpdateRequest(AuthValidator::normalizePhone)
            return safeApiCall(json) { profileApiService.updateMyProfile(request) }
                .map { dto -> store(user, dto) }
        }

        private suspend fun store(
            user: AuthUser,
            dto: UserProfileDto,
        ): UserProfile {
            val profile = dto.toDomain()
            cache.value = CachedProfile(userId = user.id, profile = profile)
            // The top bar and the Profile tab read the name from the session.
            if (profile.fullName != user.fullName) {
                sessionTokenStore.updateUser(user.copy(fullName = profile.fullName))
            }
            return profile
        }
    }
