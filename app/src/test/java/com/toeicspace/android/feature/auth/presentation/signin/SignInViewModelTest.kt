package com.toeicspace.android.feature.auth.presentation.signin

import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.testing.FakeAuthRepository
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignInViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val sessionRepository = FakeSessionRepository(isOnboardingCompleted = true)

    private fun createViewModel(
        email: String = "",
        notice: SignInNotice? = null,
    ) = SignInViewModel(email, notice, authRepository, sessionRepository)

    @Test
    fun `invalid input is rejected on the device without calling the backend`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onEmailChange("oysteic@")

            viewModel.onSubmit()

            assertEquals(FieldError.InvalidEmail, viewModel.uiState.value.emailError)
            assertEquals(FieldError.Required, viewModel.uiState.value.passwordError)
            assertTrue(authRepository.calls.isEmpty())
        }

    @Test
    fun `successful sign in goes home`() =
        runTest {
            val viewModel = createViewModel(email = "oysteic@toeicspace.vn")
            viewModel.onPasswordChange("Abcdefg1!")

            viewModel.onSubmit()

            assertEquals(SignInNextStep.Home, viewModel.uiState.value.nextStep)
            assertFalse(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun `wrong credentials show a message`() =
        runTest {
            authRepository.signInResult =
                AppResult.Failure(AppError.Server(code = "AUTH_INVALID_CREDENTIALS", status = 401))
            val viewModel = createViewModel(email = "oysteic@toeicspace.vn")
            viewModel.onPasswordChange("wrong")

            viewModel.onSubmit()

            assertEquals(
                R.string.auth_error_invalid_credentials,
                viewModel.uiState.value.errorMessage,
            )
            assertEquals(null, viewModel.uiState.value.nextStep)
        }

    @Test
    fun `unverified email sends a new code and opens verification`() =
        runTest {
            authRepository.signInResult =
                AppResult.Failure(AppError.Server(code = "AUTH_EMAIL_NOT_VERIFIED", status = 403))
            val viewModel = createViewModel(email = " oysteic@toeicspace.vn ")
            viewModel.onPasswordChange("Abcdefg1!")

            viewModel.onSubmit()

            assertEquals(
                SignInNextStep.VerifyEmail("oysteic@toeicspace.vn", "challenge-2"),
                viewModel.uiState.value.nextStep,
            )
            assertEquals(listOf("signIn", "resendVerification"), authRepository.calls)
        }

    @Test
    fun `guest entry stores guest mode and goes home without the backend`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onContinueAsGuestClick()

            assertTrue(sessionRepository.current.isGuest)
            assertEquals(SignInNextStep.Home, viewModel.uiState.value.nextStep)
            assertTrue(authRepository.calls.isEmpty())
        }

    @Test
    fun `notice from the previous screen is shown`() {
        val viewModel =
            createViewModel(email = "oysteic@toeicspace.vn", notice = SignInNotice.EMAIL_VERIFIED)

        assertEquals(R.string.auth_notice_email_verified, viewModel.uiState.value.noticeMessage)
        assertEquals("oysteic@toeicspace.vn", viewModel.uiState.value.email)
    }
}
