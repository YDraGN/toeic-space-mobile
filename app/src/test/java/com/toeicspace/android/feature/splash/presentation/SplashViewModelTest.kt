package com.toeicspace.android.feature.splash.presentation

import com.toeicspace.android.testing.FakeNetworkMonitor
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val networkMonitor = FakeNetworkMonitor()

    @Test
    fun `start destination is onboarding when onboarding is not completed`() =
        runTest {
            val viewModel = createViewModel(isOnboardingCompleted = false)

            assertEquals(
                SplashUiState.Ready(StartDestination.ONBOARDING),
                viewModel.uiState.value,
            )
        }

    @Test
    fun `start destination is home when onboarding is completed`() =
        runTest {
            val viewModel = createViewModel(isOnboardingCompleted = true)

            assertEquals(SplashUiState.Ready(StartDestination.HOME), viewModel.uiState.value)
        }

    @Test
    fun `emits network error when offline`() =
        runTest {
            networkMonitor.setOnline(false)

            val viewModel = createViewModel()

            assertEquals(SplashUiState.NetworkError, viewModel.uiState.value)
        }

    @Test
    fun `retry resolves start destination once the network is back`() =
        runTest {
            networkMonitor.setOnline(false)
            val viewModel = createViewModel(isOnboardingCompleted = true)

            networkMonitor.setOnline(true)
            viewModel.onRetryClick()

            assertEquals(SplashUiState.Ready(StartDestination.HOME), viewModel.uiState.value)
        }

    @Test
    fun `retry keeps network error while still offline`() =
        runTest {
            networkMonitor.setOnline(false)
            val viewModel = createViewModel()

            viewModel.onRetryClick()

            assertEquals(SplashUiState.NetworkError, viewModel.uiState.value)
        }

    private fun createViewModel(isOnboardingCompleted: Boolean = false) =
        SplashViewModel(
            networkMonitor = networkMonitor,
            sessionRepository = FakeSessionRepository(isOnboardingCompleted),
        )
}
