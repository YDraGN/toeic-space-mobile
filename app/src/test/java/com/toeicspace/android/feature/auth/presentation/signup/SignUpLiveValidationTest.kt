package com.toeicspace.android.feature.auth.presentation.signup

import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.testing.FakeAuthRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignUpLiveValidationTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val viewModel = SignUpViewModel(FakeAuthRepository())

    private fun kotlinx.coroutines.test.TestScope.pause() {
        advanceTimeBy(LIVE_VALIDATION_DEBOUNCE_MILLIS)
        runCurrent()
    }

    @Test
    fun `a field is checked only after typing pauses`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.onEmailChange("oysteic@")
            advanceTimeBy(LIVE_VALIDATION_DEBOUNCE_MILLIS - 1)
            runCurrent()
            assertNull(viewModel.uiState.value.emailError)

            // Typing again restarts the wait.
            viewModel.onEmailChange("oysteic@toeic")
            advanceTimeBy(LIVE_VALIDATION_DEBOUNCE_MILLIS - 1)
            runCurrent()
            assertNull(viewModel.uiState.value.emailError)

            pause()
            assertEquals(FieldError.InvalidEmail, viewModel.uiState.value.emailError)
        }

    @Test
    fun `a value that passes turns the field valid and clears its error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.onFullNameChange("")
            pause()
            assertEquals(FieldError.Required, viewModel.uiState.value.fullNameError)

            viewModel.onFullNameChange("Oysteic")
            pause()

            val state = viewModel.uiState.value
            assertNull(state.fullNameError)
            assertTrue(SignUpField.FULL_NAME in state.validFields)
        }

    @Test
    fun `fixing the password re-checks the confirmation`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.onPasswordChange("Abcdefg1!")
            viewModel.onConfirmPasswordChange("Abcdefg1?")
            pause()
            assertEquals(
                FieldError.PasswordsDoNotMatch,
                viewModel.uiState.value.confirmPasswordError,
            )

            // The user corrects the password, not the confirmation.
            viewModel.onPasswordChange("Abcdefg1?")
            pause()

            val state = viewModel.uiState.value
            assertNull(state.confirmPasswordError)
            assertTrue(SignUpField.CONFIRM_PASSWORD in state.validFields)
            assertTrue(SignUpField.PASSWORD in state.validFields)
        }

    @Test
    fun `an empty optional phone is neither an error nor valid`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.onPhoneChange("09")
            pause()
            assertEquals(FieldError.InvalidPhone, viewModel.uiState.value.phoneError)

            viewModel.onPhoneChange("")
            pause()

            val state = viewModel.uiState.value
            assertNull(state.phoneError)
            assertFalse(SignUpField.PHONE in state.validFields)
        }

    @Test
    fun `submitting checks at once without waiting for the pause`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.onPasswordChange("Abcdefg1!")
            viewModel.onConfirmPasswordChange("different")

            viewModel.onSubmit()

            assertEquals(
                FieldError.PasswordsDoNotMatch,
                viewModel.uiState.value.confirmPasswordError,
            )
            assertEquals(FieldError.Required, viewModel.uiState.value.fullNameError)
        }
}
