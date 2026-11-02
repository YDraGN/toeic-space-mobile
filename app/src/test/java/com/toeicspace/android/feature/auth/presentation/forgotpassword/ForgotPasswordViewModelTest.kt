package com.toeicspace.android.feature.auth.presentation.forgotpassword

import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.testing.FakeAuthRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ForgotPasswordViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()

    // Created lazily: the constructor touches viewModelScope, which needs the test Main dispatcher.
    private val viewModel by lazy {
        ForgotPasswordViewModel(
            "oysteic@toeicspace.vn",
            authRepository,
        )
    }

    private fun reachNewPasswordStep() {
        viewModel.onSubmit()
        viewModel.onOtpChange("123456")
    }

    @Test
    fun `email, code and new password complete the reset with the token from verification`() =
        runTest {
            reachNewPasswordStep()
            assertEquals(ForgotPasswordStep.NEW_PASSWORD, viewModel.uiState.value.step)

            viewModel.onNewPasswordChange("Abcdefgh!")
            viewModel.onConfirmPasswordChange("Abcdefgh!")
            viewModel.onSubmit()

            assertTrue(viewModel.uiState.value.isCompleted)
            assertEquals("reset-token", authRepository.lastResetToken)
        }

    @Test
    fun `weak new password is rejected on the device`() =
        runTest {
            reachNewPasswordStep()

            viewModel.onNewPasswordChange("abc")
            viewModel.onSubmit()

            assertEquals(
                listOf("requestPasswordReset", "verifyPasswordReset"),
                authRepository.calls,
            )
        }

    @Test
    fun `expired reset session starts again from the email step`() =
        runTest {
            authRepository.confirmPasswordResetResult =
                AppResult.Failure(AppError.Server(code = "TOKEN_EXPIRED", status = 400))
            reachNewPasswordStep()
            viewModel.onNewPasswordChange("Abcdefgh!")
            viewModel.onConfirmPasswordChange("Abcdefgh!")

            viewModel.onSubmit()

            assertEquals(ForgotPasswordStep.EMAIL, viewModel.uiState.value.step)
            assertEquals(
                R.string.auth_error_reset_session_expired,
                viewModel.uiState.value.errorMessage,
            )
        }
}
