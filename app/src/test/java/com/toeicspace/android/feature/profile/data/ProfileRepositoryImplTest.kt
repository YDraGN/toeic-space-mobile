package com.toeicspace.android.feature.profile.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.datastore.AccessToken
import com.toeicspace.android.core.datastore.SessionTokens
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.feature.profile.data.remote.ProfileApiService
import com.toeicspace.android.feature.profile.data.remote.UpdateProfileRequestDto
import com.toeicspace.android.feature.profile.data.remote.UserProfileDto
import com.toeicspace.android.feature.profile.data.repository.ProfileRepositoryImpl
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.Gender
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.FakeSessionTokenStore
import com.toeicspace.android.testing.TestUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
                )
            return profile
        }
    }

    private fun signedInSession() =
        FakeSessionRepository(isOnboardingCompleted = true, user = TestUser)

    private fun repository(
        api: ProfileApiService = FakeProfileApi(serverProfile),
        session: SessionRepository = signedInSession(),
        tokenStore: FakeSessionTokenStore = FakeSessionTokenStore(),
    ) = ProfileRepositoryImpl(api, session, tokenStore, json)

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
