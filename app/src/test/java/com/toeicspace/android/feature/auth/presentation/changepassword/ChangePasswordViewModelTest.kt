package com.toeicspace.android.feature.auth.presentation.changepassword

import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.testing.FakeAuthRepository
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.TestUser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChangePasswordViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()

    private fun viewModel() =
        ChangePasswordViewModel(repository, FakeSessionRepository(user = TestUser)).apply {
            onCurrentPasswordChange("Old@1234")
            onNewPasswordChange("New@1234")
            onConfirmPasswordChange("New@1234")
        }

    @Test
    fun `invalid passwords are caught on the device`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onNewPasswordChange("Old@1234")
            viewModel.onConfirmPasswordChange("other")

            viewModel.onSubmit()

            val state = viewModel.uiState.value
            assertEquals(FieldError.PasswordSameAsOld, state.newPasswordError)
            assertEquals(FieldError.PasswordsDoNotMatch, state.confirmPasswordError)
            assertTrue(repository.calls.isEmpty())
        }

    @Test
    fun `valid passwords request an OTP and show the account email`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onSubmit()

            val state = viewModel.uiState.value
            assertEquals(ChangePasswordStep.OTP, state.step)
            assertEquals(TestUser.email, state.email)
            assertEquals(60, state.resendInSeconds)
        }

    @Test
    fun `going back during the cooldown reuses the code already sent`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onSubmit()
            viewModel.onEditPasswordsClick()

            viewModel.onSubmit()

            assertEquals(ChangePasswordStep.OTP, viewModel.uiState.value.step)
            assertEquals(listOf("requestChangePasswordOtp"), repository.calls)
        }

    @Test
    fun `wrong current password returns to the passwords step`() =
        runTest {
            repository.requestChangePasswordOtpResult =
                AppResult.Failure(AppError.Server("AUTH_INVALID_CREDENTIALS", 400))
            val viewModel = viewModel()

            viewModel.onSubmit()

            val state = viewModel.uiState.value
            assertEquals(ChangePasswordStep.PASSWORDS, state.step)
            assertEquals(R.string.auth_error_current_password_incorrect, state.errorMessage)
        }

    @Test
    fun `six digits submit the change and complete`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onSubmit()

            viewModel.onOtpChange("123456")

            assertEquals(
                "changePassword:Old@1234:New@1234:New@1234:123456",
                repository.calls.last(),
            )
            assertTrue(viewModel.uiState.value.isCompleted)
        }

    @Test
    fun `wrong OTP clears the code and stays on the OTP step`() =
        runTest {
            repository.changePasswordResult =
                AppResult.Failure(AppError.Server("OTP_INVALID_OR_EXPIRED", 400))
            val viewModel = viewModel()
            viewModel.onSubmit()

            viewModel.onOtpChange("123456")

            val state = viewModel.uiState.value
            assertEquals(ChangePasswordStep.OTP, state.step)
            assertEquals("", state.otp)
            assertEquals(R.string.auth_error_otp_invalid, state.errorMessage)
            assertFalse(state.isCompleted)
        }

    @Test
    fun `an expired session asks the user to sign in again`() =
        runTest {
            repository.requestChangePasswordOtpResult =
                AppResult.Failure(AppError.Server(null, 401))
            val viewModel = viewModel()

            viewModel.onSubmit()

            assertTrue(viewModel.uiState.value.isSessionEnded)
            viewModel.onNavigationHandled()
            assertFalse(viewModel.uiState.value.isSessionEnded)
            assertNull(viewModel.uiState.value.errorMessage)
        }
}
