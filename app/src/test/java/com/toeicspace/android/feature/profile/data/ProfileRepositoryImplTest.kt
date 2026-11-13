package com.toeicspace.android.feature.profile.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.datastore.AccessToken
import com.toeicspace.android.core.datastore.SessionTokens
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.feature.profile.data.avatar.AvatarImageEncoder
import com.toeicspace.android.feature.profile.data.avatar.AvatarStorageConfig
import com.toeicspace.android.feature.profile.data.remote.AvatarUploadRequestDto
import com.toeicspace.android.feature.profile.data.remote.AvatarUploadResponseDto
import com.toeicspace.android.feature.profile.data.remote.ProfileApiService
import com.toeicspace.android.feature.profile.data.remote.UpdateProfileRequestDto
import com.toeicspace.android.feature.profile.data.remote.UserProfileDto
import com.toeicspace.android.feature.profile.data.repository.ProfileRepositoryImpl
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.Gender
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.FakeSessionTokenStore
import com.toeicspace.android.testing.TestUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class ProfileRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    private val serverProfile =
        UserProfileDto(
            id = TestUser.id,
            fullName = TestUser.fullName,
            email = TestUser.email,
            phone = "0905123456",
            dateOfBirth = "2004-05-12",
            gender = JsonPrimitive(2),
            targetScore = 650,
            currentLevel = JsonPrimitive("Intermediate"),
        )

    private class FakeProfileApi(
        var profile: UserProfileDto,
    ) : ProfileApiService {
        var lastRequest: UpdateProfileRequestDto? = null
        var failWith: Exception? = null
        var uploadUrl: String = "https://storage.test/bucket/avatars/user-1?X-Amz-Signature=abc"
        var avatarUploadRequests = 0

        override suspend fun getMyProfile(): UserProfileDto {
            failWith?.let { throw it }
            return profile
        }

        override suspend fun updateMyProfile(body: UpdateProfileRequestDto): UserProfileDto {
            lastRequest = body
            failWith?.let { throw it }
            profile =
                profile.copy(
                    fullName = body.fullName,
                    phone = body.phone,
                    targetScore = body.targetScore,
                    gender = body.gender?.let(::JsonPrimitive),
                    avatarUrl = body.avatarUrl,
                )
            return profile
        }

        override suspend fun createAvatarUploadUrl(
            body: AvatarUploadRequestDto,
        ): AvatarUploadResponseDto {
            avatarUploadRequests++
            return AvatarUploadResponseDto(uploadUrl)
        }
    }

    private val storage = MockWebServer()

    @Before
    fun startStorage() = storage.start()

    @After
    fun stopStorage() = storage.close()

    private fun signedInSession() =
        FakeSessionRepository(isOnboardingCompleted = true, user = TestUser)

    private fun repository(
        api: ProfileApiService = FakeProfileApi(serverProfile),
        session: SessionRepository = signedInSession(),
        tokenStore: FakeSessionTokenStore = FakeSessionTokenStore(),
        encoder: AvatarImageEncoder = AvatarImageEncoder { ImageBytes },
    ) = ProfileRepositoryImpl(
        profileApiService = api,
        sessionRepository = session,
        sessionTokenStore = tokenStore,
        json = json,
        avatarImageEncoder = encoder,
        storageClient = OkHttpClient(),
        avatarStorage = AvatarStorageConfig(cdnBaseUrl = "https://cdn.test/"),
        clock = { 42L },
        ioDispatcher = Dispatchers.Unconfined,
    )

    @Test
    fun `avatar is uploaded to storage then saved with its public url`() =
        runTest {
            storage.enqueue(MockResponse.Builder().code(200).build())
            val api =
                FakeProfileApi(serverProfile).apply {
                    uploadUrl =
                        storage
                            .url(
                                "/toeic-space-media/avatars/user-1?X-Amz-Signature=abc",
                            ).toString()
                }
            val repository = repository(api = api)

            val result = repository.updateAvatar("content://picked/1")

            val upload = storage.takeRequest()
            assertEquals("PUT", upload.method)
            assertEquals("image/webp", upload.headers["Content-Type"])
            // A bearer token next to the presigned signature would make R2 refuse the upload.
            assertNull(upload.headers["Authorization"])
            assertEquals(ImageBytes.toList(), upload.body?.toByteArray()?.toList())
            val saved = api.lastRequest!!
            assertEquals("https://cdn.test/avatars/user-1?v=42", saved.avatarUrl)
            // PUT /users/me replaces everything, so the rest of the profile is sent unchanged.
            assertEquals(TestUser.fullName, saved.fullName)
            assertEquals(650, saved.targetScore)
            assertEquals(
                "https://cdn.test/avatars/user-1?v=42",
                (result as AppResult.Success).data.avatarUrl,
            )
            assertFalse(repository.isUploadingAvatar.first())
        }

    @Test
    fun `a rejected upload keeps the old avatar`() =
        runTest {
            storage.enqueue(MockResponse.Builder().code(403).build())
            val api =
                FakeProfileApi(serverProfile).apply {
                    uploadUrl = storage.url("/bucket/avatars/user-1").toString()
                }

            val result = repository(api = api).updateAvatar("content://picked/1")

            assertEquals(AppResult.Failure(AppError.Server(code = null, status = 403)), result)
            assertNull(api.lastRequest)
        }

    @Test
    fun `an unreadable image fails before asking for an upload url`() =
        runTest {
            val api = FakeProfileApi(serverProfile)

            val result = repository(api = api, encoder = { null }).updateAvatar("content://broken")

            assertEquals(AppResult.Failure(AppError.Unknown), result)
            assertEquals(0, api.avatarUploadRequests)
        }

    @Test
    fun `before any fetch the session's name and email stand in`() =
        runTest {
            val profile = repository().profile.first()

            assertEquals(TestUser.fullName, profile?.fullName)
            assertEquals(TestUser.email, profile?.email)
            assertNull(profile?.targetScore)
        }

    @Test
    fun `refresh maps the server profile, numeric and named enums alike`() =
        runTest {
            val repository = repository()

            assertEquals(AppResult.Success(Unit), repository.refresh())

            val profile = repository.profile.first()
            assertEquals(Gender.FEMALE, profile?.gender)
            assertEquals(EnglishLevel.INTERMEDIATE, profile?.currentLevel)
            assertEquals(LocalDate.of(2004, 5, 12), profile?.dateOfBirth)
            assertEquals(650, profile?.targetScore)
        }

    @Test
    fun `a failed refresh keeps the stand-in profile`() =
        runTest {
            val api = FakeProfileApi(serverProfile).apply { failWith = IOException() }
            val repository = repository(api = api)

            assertEquals(AppResult.Failure(AppError.ServerUnreachable), repository.refresh())
            assertEquals(TestUser.fullName, repository.profile.first()?.fullName)
        }

    @Test
    fun `update sends every field and syncs a new name into the session`() =
        runTest {
            val api = FakeProfileApi(serverProfile)
            val tokenStore =
                FakeSessionTokenStore().apply {
                    save(
                        SessionTokens(AccessToken("a", Long.MAX_VALUE), "r", Long.MAX_VALUE),
                        TestUser,
                    )
                }
            val repository = repository(api = api, tokenStore = tokenStore)
            repository.refresh()
            val current = repository.profile.first()!!

            val result =
                repository.updateProfile(
                    current.copy(
                        fullName = "Minh Anh",
                        phone = "+84 905 123 456",
                        gender = Gender.OTHER,
                    ),
                )

            val request = api.lastRequest!!
            assertEquals("Minh Anh", request.fullName)
            assertEquals("0905123456", request.phone)
            assertEquals("2004-05-12", request.dateOfBirth)
            assertEquals(3, request.gender)
            assertEquals(2, request.currentLevel)
            assertEquals(650, request.targetScore)
            assertEquals("Minh Anh", (result as AppResult.Success).data.fullName)
            assertEquals("Minh Anh", tokenStore.user.first()?.fullName)
        }

    @Test
    fun `guests have no profile and cannot load one`() =
        runTest {
            val guest: SessionRepository =
                object : SessionRepository by FakeSessionRepository() {
                    override val session: Flow<SessionState> =
                        flowOf(
                            SessionState.Loading,
                            SessionState.Ready(
                                isOnboardingCompleted = true,
                                user = null,
                                isGuest = true,
                            ),
                        )
                }
            val repository = repository(session = guest)

            assertNull(repository.profile.first())
            assertEquals(AppResult.Failure(AppError.Unauthorized), repository.refresh())
        }
}

private val ImageBytes = byteArrayOf(1, 2, 3, 4)
