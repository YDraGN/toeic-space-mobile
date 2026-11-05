package com.toeicspace.android.feature.profile.data

import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.TestUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SampleProfileRepositoryTest {
    private fun sessionOf(vararg states: SessionState): SessionRepository =
        object : SessionRepository by FakeSessionRepository() {
            override val session: Flow<SessionState> = flowOf(*states)
        }

    @Test
    fun `a session still loading is not mistaken for a guest`() =
        runTest {
            val ready =
                SessionState.Ready(
                    isOnboardingCompleted = true,
                    user = TestUser,
                    isGuest = false,
                )
            val repository = SampleProfileRepository(sessionOf(SessionState.Loading, ready))

            val emitted = repository.profile.toList()

            assertEquals(1, emitted.size)
            assertEquals(TestUser.fullName, emitted.single()?.fullName)
        }

    @Test
    fun `guests have no profile`() =
        runTest {
            val guest =
                SessionState.Ready(
                    isOnboardingCompleted = true,
                    user = null,
                    isGuest = true,
                )
            val repository = SampleProfileRepository(sessionOf(guest))

            assertEquals(listOf(null), repository.profile.toList())
        }
}
