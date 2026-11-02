package com.toeicspace.android.feature.auth.presentation.forgotpassword

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.auth.domain.AuthRepository
import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.auth.presentation.common.ResendCountdown
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    EMAIL,
    OTP,
    NEW_PASSWORD,
}

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.EMAIL,
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val emailError: FieldError? = null,
    val otpError: FieldError? = null,
    val newPasswordError: FieldError? = null,
    val confirmPasswordError: FieldError? = null,
    @param:StringRes val errorMessage: Int? = null,
    @param:StringRes val noticeMessage: Int? = null,
    val resendInSeconds: Int = 0,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
) {
    val canResend: Boolean get() = resendInSeconds == 0 && !isSubmitting
}

private val RESET_SESSION_ERRORS = setOf("TOKEN_INVALID", "TOKEN_EXPIRED", "TOKEN_REVOKED")

@HiltViewModel(assistedFactory = ForgotPasswordViewModel.Factory::class)
class ForgotPasswordViewModel
    @AssistedInject
    constructor(
        @Assisted initialEmail: String,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(initialEmail: String): ForgotPasswordViewModel
        }

        private val _uiState = MutableStateFlow(ForgotPasswordUiState(email = initialEmail))
        val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

        // Authorises the new password; kept out of the UI state so it is never rendered or logged.
        private var resetToken: String? = null

        private val countdown =
            ResendCountdown(viewModelScope) { seconds ->
                _uiState.update {
                    it.copy(resendInSeconds = seconds)
                }
            }

        fun onEmailChange(value: String) =
            _uiState.update {
                it.copy(email = value, emailError = null, errorMessage = null)
            }

        fun onOtpChange(value: String) {
            val digits = value.filter(Char::isDigit).take(AuthValidator.OTP_LENGTH)
            _uiState.update { it.copy(otp = digits, otpError = null, errorMessage = null) }
            if (digits.length == AuthValidator.OTP_LENGTH) onSubmit()
        }

        fun onNewPasswordChange(value: String) =
            _uiState.update {
                it.copy(
                    newPassword = value,
                    newPasswordError = null,
                    errorMessage = null,
                )
            }

        fun onConfirmPasswordChange(value: String) =
            _uiState.update {
                it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null)
            }

        fun onSubmit() {
            val state = _uiState.value
            if (state.isSubmitting) return
            when (state.step) {
                ForgotPasswordStep.EMAIL -> requestOtp(state)
                ForgotPasswordStep.OTP -> verifyOtp(state)
                ForgotPasswordStep.NEW_PASSWORD -> confirmNewPassword(state)
            }
        }

        fun onResendClick() {
            val state = _uiState.value
            if (state.step == ForgotPasswordStep.OTP && state.canResend) requestOtp(state)
        }

        fun onChangeEmailClick() {
            if (_uiState.value.isSubmitting) return
            resetToken = null
            _uiState.update {
                it.copy(
                    step = ForgotPasswordStep.EMAIL,
                    otp = "",
                    errorMessage = null,
                    noticeMessage = null,
                )
            }
        }

        fun onNavigationHandled() = _uiState.update { it.copy(isCompleted = false) }

        private fun requestOtp(state: ForgotPasswordUiState) {
            AuthValidator.validateEmail(state.email)?.let { error ->
                _uiState.update { it.copy(emailError = error) }
                return
            }
            submit {
                when (val result = authRepository.requestPasswordReset(state.email)) {
                    is AppResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                step = ForgotPasswordStep.OTP,
                                otp = "",
                                noticeMessage =
                                    if (state.step ==
                                        ForgotPasswordStep.OTP
                                    ) {
                                        R.string.auth_notice_otp_resent
                                    } else {
                                        null
                                    },
                            )
                        }
                        countdown.start(result.data)
                    }
                    is AppResult.Failure -> fail(result.error)
                }
            }
        }

        private fun verifyOtp(state: ForgotPasswordUiState) {
            AuthValidator.validateOtp(state.otp)?.let { error ->
                _uiState.update { it.copy(otpError = error) }
                return
            }
            submit {
                when (val result = authRepository.verifyPasswordReset(state.email, state.otp)) {
                    is AppResult.Success -> {
                        resetToken = result.data
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                step = ForgotPasswordStep.NEW_PASSWORD,
                            )
                        }
                    }
                    is AppResult.Failure -> {
                        _uiState.update { it.copy(otp = "") }
                        fail(result.error)
                    }
                }
            }
        }

        private fun confirmNewPassword(state: ForgotPasswordUiState) {
            val newPasswordError = AuthValidator.validateResetPassword(state.newPassword)
            val confirmError =
                AuthValidator.validateConfirmPassword(
                    state.newPassword,
                    state.confirmPassword,
                )
            if (newPasswordError != null || confirmError != null) {
                _uiState.update {
                    it.copy(
                        newPasswordError = newPasswordError,
                        confirmPasswordError = confirmError,
                    )
                }
                return
            }
            val token = resetToken ?: return restartFromEmail()

            submit {
                when (
                    val result =
                        authRepository.confirmPasswordReset(
                            token,
                            state.newPassword,
                            state.confirmPassword,
                        )
                ) {
                    is AppResult.Success -> {
                        resetToken = null
                        _uiState.update { it.copy(isSubmitting = false, isCompleted = true) }
                    }
                    is AppResult.Failure ->
                        if (result.error.isResetSessionError) {
                            restartFromEmail()
                        } else {
                            fail(
                                result.error,
                            )
                        }
                }
            }
        }

        // The reset token expired or was used: the only way forward is a new OTP.
        private fun restartFromEmail() {
            resetToken = null
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    step = ForgotPasswordStep.EMAIL,
                    otp = "",
                    newPassword = "",
                    confirmPassword = "",
                    errorMessage = R.string.auth_error_reset_session_expired,
                )
            }
        }

        private fun submit(block: suspend () -> Unit) {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    noticeMessage = null,
                )
            }
            viewModelScope.launch { block() }
        }

        private fun fail(error: AppError) =
            _uiState.update {
                it.copy(isSubmitting = false, errorMessage = error.messageRes())
            }

        private val AppError.isResetSessionError: Boolean
            get() = this is AppError.Server && code in RESET_SESSION_ERRORS
    }
