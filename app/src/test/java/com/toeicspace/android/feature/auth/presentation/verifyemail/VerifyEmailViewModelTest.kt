package com.toeicspace.android.feature.auth.presentation.verifyemail

import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.testing.FakeAuthRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyEmailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val authRepository = FakeAuthRepository()

    private fun createViewModel() =
        VerifyEmailViewModel("oysteic@toeicspace.vn", "challenge-1", authRepository)

    @Test
    fun `typing the sixth digit submits the code`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onOtpChange("12a3456")
            runCurrent()

            assertTrue(viewModel.uiState.value.isVerified)
            assertEquals(listOf("verifyEmail:challenge-1:123456"), authRepository.calls)
        }

    @Test
    fun `wrong code clears the field and shows a message`() =
        runTest(mainDispatcherRule.testDispatcher) {
            authRepository.verifyEmailResult =
                AppResult.Failure(AppError.Server(code = "OTP_INVALID_OR_EXPIRED", status = 400))
            val viewModel = createViewModel()

            viewModel.onOtpChange("123456")
            runCurrent()

            assertEquals("", viewModel.uiState.value.otp)
            assertEquals(R.string.auth_error_otp_invalid, viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `resend is blocked during the cooldown and uses the new challenge afterwards`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            runCurrent()

            viewModel.onResendClick()
            runCurrent()
            assertFalse(viewModel.uiState.value.canResend)
            assertTrue(authRepository.calls.isEmpty())

            advanceTimeBy(60_001)
            viewModel.onResendClick()
            runCurrent()

            assertEquals("challenge-2", viewModel.uiState.value.challengeId)
            assertEquals(listOf("resendVerification"), authRepository.calls)
        }
}
