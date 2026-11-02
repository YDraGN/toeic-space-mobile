package com.toeicspace.android.feature.auth.presentation.signup

import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.testing.FakeAuthRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val viewModel = SignUpViewModel(authRepository)

    private fun fillValidForm() {
        viewModel.onFullNameChange("Oysteic")
        viewModel.onEmailChange("oysteic@toeicspace.vn")
        viewModel.onPasswordChange("Abcdefg1!")
        viewModel.onConfirmPasswordChange("Abcdefg1!")
    }

    @Test
    fun `every invalid field is reported and nothing is sent`() =
        runTest {
            viewModel.onPhoneChange("12345")
            viewModel.onPasswordChange("weak")
            viewModel.onConfirmPasswordChange("other")

            viewModel.onSubmit()

            val state = viewModel.uiState.value
            assertEquals(FieldError.Required, state.fullNameError)
            assertEquals(FieldError.Required, state.emailError)
            assertEquals(FieldError.InvalidPhone, state.phoneError)
            assertEquals(FieldError.PasswordTooShort, state.passwordError)
            assertEquals(FieldError.PasswordsDoNotMatch, state.confirmPasswordError)
            assertTrue(authRepository.calls.isEmpty())
        }

    @Test
    fun `valid form registers and opens verification`() =
        runTest {
            fillValidForm()

            viewModel.onSubmit()

            assertEquals(
                PendingVerification("oysteic@toeicspace.vn", "challenge-1"),
                viewModel.uiState.value.verification,
            )
            assertNull(authRepository.lastRegistration?.phone)
        }

    @Test
    fun `rejected registration shows a generic message`() =
        runTest {
            authRepository.registerResult =
                AppResult.Failure(
                    AppError.Server(code = "REGISTRATION_COULD_NOT_BE_COMPLETED", status = 409),
                )
            fillValidForm()

            viewModel.onSubmit()

            assertEquals(
                R.string.auth_error_registration_failed,
                viewModel.uiState.value.errorMessage,
            )
        }

    @Test
    fun `backend field errors are attached to their fields`() =
        runTest {
            authRepository.registerResult =
                AppResult.Failure(
                    AppError.Validation(mapOf("email" to listOf("Email is invalid."))),
                )
            fillValidForm()

            viewModel.onSubmit()

            assertEquals(setOf("email"), viewModel.uiState.value.serverRejectedFields)
            assertNull(viewModel.uiState.value.errorMessage)
        }
}
