package com.toeicspace.android.feature.profile.data.repository

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.common.Dispatcher
import com.toeicspace.android.core.common.ToeicDispatchers
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.datastore.SessionTokenStore
import com.toeicspace.android.core.di.SessionHttpClient
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.network.NoConnectivityException
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.profile.data.avatar.AVATAR_CONTENT_TYPE
import com.toeicspace.android.feature.profile.data.avatar.AvatarImageEncoder
import com.toeicspace.android.feature.profile.data.avatar.AvatarStorageConfig
import com.toeicspace.android.feature.profile.data.avatar.publicAvatarUrl
import com.toeicspace.android.feature.profile.data.remote.AvatarUploadRequestDto
import com.toeicspace.android.feature.profile.data.remote.ProfileApiService
import com.toeicspace.android.feature.profile.data.remote.UserProfileDto
import com.toeicspace.android.feature.profile.data.remote.toDomain
import com.toeicspace.android.feature.profile.data.remote.toUpdateRequest
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
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
        private val avatarImageEncoder: AvatarImageEncoder,
        // No auth interceptor: a bearer token next to the presigned signature makes R2 refuse the PUT.
        @param:SessionHttpClient private val storageClient: OkHttpClient,
        private val avatarStorage: AvatarStorageConfig,
        private val clock: Clock,
        @param:Dispatcher(ToeicDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : ProfileRepository {
        private val cache = MutableStateFlow<CachedProfile?>(null)
        private val uploadingAvatar = MutableStateFlow(false)

        override val isUploadingAvatar: Flow<Boolean> = uploadingAvatar

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

        override suspend fun updateAvatar(imageUri: String): AppResult<UserProfile> {
            val user = signedInUser.first() ?: return AppResult.Failure(AppError.Unauthorized)
            uploadingAvatar.value = true
            try {
                val image =
                    avatarImageEncoder.encode(imageUri)
                        ?: return AppResult.Failure(AppError.Unknown)
                val uploadUrl =
                    when (
                        val result =
                            safeApiCall(json) {
                                profileApiService.createAvatarUploadUrl(
                                    AvatarUploadRequestDto(AVATAR_CONTENT_TYPE),
                                )
                            }
                    ) {
                        is AppResult.Success -> result.data.uploadUrl
                        is AppResult.Failure -> return result
                    }
                uploadToStorage(uploadUrl, image)?.let { return AppResult.Failure(it) }

                // PUT /users/me replaces everything, so start from the full profile, not the stand-in.
                val current =
                    cache.value?.takeIf { it.userId == user.id }?.profile
                        ?: when (
                            val fetched =
                                safeApiCall(
                                    json,
                                ) { profileApiService.getMyProfile() }
                        ) {
                            is AppResult.Success -> store(user, fetched.data)
                            is AppResult.Failure -> return fetched
                        }
                val avatarUrl =
                    publicAvatarUrl(
                        uploadUrl,
                        avatarStorage.cdnBaseUrl,
                        version = clock.nowMillis(),
                    )
                return updateProfile(current.copy(avatarUrl = avatarUrl))
            } finally {
                uploadingAvatar.value = false
            }
        }

        /** PUTs the image straight to the presigned URL; returns the failure, or null on success. */
        private suspend fun uploadToStorage(
            uploadUrl: String,
            image: ByteArray,
        ): AppError? =
            withContext(ioDispatcher) {
                val request =
                    Request
                        .Builder()
                        .url(uploadUrl)
                        .put(image.toRequestBody(AVATAR_CONTENT_TYPE.toMediaType()))
                        .build()
                try {
                    storageClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            null
                        } else {
                            AppError.Server(
                                code = null,
                                status = response.code,
                            )
                        }
                    }
                } catch (_: NoConnectivityException) {
                    AppError.Network
                } catch (_: IOException) {
                    AppError.ServerUnreachable
                } catch (_: IllegalArgumentException) {
                    // Not a valid URL.
                    AppError.Unknown
                }
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
