package com.toeicspace.android.feature.splash.presentation

import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.TestUser
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `start destination is onboarding when onboarding is not completed`() =
        runTest {
            val viewModel = createViewModel(FakeSessionRepository(isOnboardingCompleted = false))

            advanceUntilIdle()

            assertEquals(SplashUiState.Ready(StartDestination.ONBOARDING), viewModel.uiState.value)
        }

    @Test
    fun `splash stays for the minimum time even when the session is ready at once`() =
        runTest {
            val viewModel = createViewModel(FakeSessionRepository(isOnboardingCompleted = true))

            advanceTimeBy(MIN_SPLASH_MILLIS - 1)
            runCurrent()
            assertEquals(SplashUiState.Loading, viewModel.uiState.value)

            advanceTimeBy(1)
            runCurrent()
            assertEquals(SplashUiState.Ready(StartDestination.SIGN_IN), viewModel.uiState.value)
        }

    @Test
    fun `start destination is sign in when onboarding is done but nobody is signed in`() =
        runTest {
            val viewModel = createViewModel(FakeSessionRepository(isOnboardingCompleted = true))

            advanceUntilIdle()

            assertEquals(SplashUiState.Ready(StartDestination.SIGN_IN), viewModel.uiState.value)
        }

    @Test
    fun `start destination is home for a cached signed-in user`() =
        runTest {
            val viewModel =
                createViewModel(
                    FakeSessionRepository(isOnboardingCompleted = true, user = TestUser),
                )
            advanceUntilIdle()

            assertEquals(SplashUiState.Ready(StartDestination.HOME), viewModel.uiState.value)
        }

    @Test
    fun `start destination is home for a guest`() =
        runTest {
            val viewModel =
                createViewModel(FakeSessionRepository(isOnboardingCompleted = true, isGuest = true))
            advanceUntilIdle()

            assertEquals(SplashUiState.Ready(StartDestination.HOME), viewModel.uiState.value)
        }

    private fun createViewModel(sessionRepository: FakeSessionRepository) =
        SplashViewModel(sessionRepository)
}
