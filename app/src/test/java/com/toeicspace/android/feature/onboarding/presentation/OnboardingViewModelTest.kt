package com.toeicspace.android.feature.onboarding.presentation

import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = FakeSessionRepository(isOnboardingCompleted = false)
    private val viewModel = OnboardingViewModel(sessionRepository)

    @Test
    fun `initial state has no pending navigation`() {
        assertEquals(OnboardingUiState(), viewModel.uiState.value)
    }

    @Test
    fun `sign up click completes onboarding and requests sign up`() =
        runTest {
            viewModel.onSignUpClick()

            assertTrue(sessionRepository.currentOnboardingCompleted)
            assertEquals(OnboardingNextStep.SIGN_UP, viewModel.uiState.value.nextStep)
            assertFalse(viewModel.uiState.value.isCompleting)
        }

    @Test
    fun `sign in click completes onboarding and requests sign in`() =
        runTest {
            viewModel.onSignInClick()

            assertTrue(sessionRepository.currentOnboardingCompleted)
            assertEquals(OnboardingNextStep.SIGN_IN, viewModel.uiState.value.nextStep)
        }

    @Test
    fun `guest click completes onboarding and requests home`() =
        runTest {
            viewModel.onContinueAsGuestClick()

            assertTrue(sessionRepository.currentOnboardingCompleted)
            assertEquals(OnboardingNextStep.GUEST, viewModel.uiState.value.nextStep)
        }

    @Test
    fun `second click is ignored while navigation is pending`() =
        runTest {
            viewModel.onSignUpClick()
            viewModel.onContinueAsGuestClick()

            assertEquals(OnboardingNextStep.SIGN_UP, viewModel.uiState.value.nextStep)
        }

    @Test
    fun `navigation handled clears the pending step`() =
        runTest {
            viewModel.onSignInClick()

            viewModel.onNavigationHandled()

            assertNull(viewModel.uiState.value.nextStep)
        }
}
