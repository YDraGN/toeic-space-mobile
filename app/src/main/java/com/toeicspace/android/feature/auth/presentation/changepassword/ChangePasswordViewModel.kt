package com.toeicspace.android.feature.auth.presentation.changepassword

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.feature.auth.domain.AuthRepository
import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.auth.presentation.common.ResendCountdown
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Same order as the web: the backend checks the current password before it sends the OTP, and the
 * OTP is only checked by the final request, so all three passwords come first.
 */
enum class ChangePasswordStep {
    PASSWORDS,
    OTP,
}

data class ChangePasswordUiState(
    val step: ChangePasswordStep = ChangePasswordStep.PASSWORDS,
    val email: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val otp: String = "",
    val currentPasswordError: FieldError? = null,
    val newPasswordError: FieldError? = null,
    val confirmPasswordError: FieldError? = null,
    val otpError: FieldError? = null,
    @param:StringRes val errorMessage: Int? = null,
    @param:StringRes val noticeMessage: Int? = null,
    val resendInSeconds: Int = 0,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val isSessionEnded: Boolean = false,
) {
    val canResend: Boolean get() = resendInSeconds == 0 && !isSubmitting
}

private const val INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS"
private const val PASSWORD_SAME_AS_OLD = "AUTH_PASSWORD_SAME_AS_OLD"
private const val HTTP_UNAUTHORIZED = 401

@HiltViewModel
class ChangePasswordViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        // Passwords and the OTP stay in memory only, like on the web: leaving the screen starts over.
        private val _uiState = MutableStateFlow(ChangePasswordUiState())
        val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

        private val countdown =
            ResendCountdown(viewModelScope) { seconds ->
                _uiState.update { it.copy(resendInSeconds = seconds) }
            }

        init {
            viewModelScope.launch {
                val session =
                    sessionRepository.session
                        .filterIsInstance<SessionState.Ready>()
                        .first()
                _uiState.update { it.copy(email = session.user?.email.orEmpty()) }
            }
        }

        fun onCurrentPasswordChange(value: String) =
            _uiState.update {
                it.copy(currentPassword = value, currentPasswordError = null, errorMessage = null)
            }

        fun onNewPasswordChange(value: String) =
            _uiState.update {
                it.copy(newPassword = value, newPasswordError = null, errorMessage = null)
            }

        fun onConfirmPasswordChange(value: String) =
            _uiState.update {
                it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null)
            }

        fun onOtpChange(value: String) {
            val digits = value.filter(Char::isDigit).take(AuthValidator.OTP_LENGTH)
            _uiState.update { it.copy(otp = digits, otpError = null, errorMessage = null) }
            if (digits.length == AuthValidator.OTP_LENGTH) onSubmit()
        }

        fun onSubmit() {
            val state = _uiState.value
            if (state.isSubmitting) return
            when (state.step) {
                ChangePasswordStep.PASSWORDS -> continueToOtp(state)
                ChangePasswordStep.OTP -> changePassword(state)
            }
        }

        fun onResendClick() {
            val state = _uiState.value
            if (state.step == ChangePasswordStep.OTP && state.canResend) requestOtp(state)
        }

        /** Back from the OTP step returns to the passwords, keeping what was typed. */
        fun onEditPasswordsClick() {
            if (_uiState.value.isSubmitting) return
            _uiState.update {
                it.copy(
                    step = ChangePasswordStep.PASSWORDS,
                    otp = "",
                    otpError = null,
                    errorMessage = null,
                    noticeMessage = null,
                )
            }
        }

        fun onNavigationHandled() =
            _uiState.update { it.copy(isCompleted = false, isSessionEnded = false) }

        private fun continueToOtp(state: ChangePasswordUiState) {
            val currentError = AuthValidator.validateSignInPassword(state.currentPassword)
            val newError =
                AuthValidator.validateResetPassword(state.newPassword)
                    ?: FieldError.PasswordSameAsOld.takeIf {
                        state.newPassword == state.currentPassword
                    }
            val confirmError =
                AuthValidator.validateConfirmPassword(state.newPassword, state.confirmPassword)
            if (currentError != null || newError != null || confirmError != null) {
                _uiState.update {
                    it.copy(
                        currentPasswordError = currentError,
                        newPasswordError = newError,
                        confirmPasswordError = confirmError,
                    )
                }
                return
            }
            // A code sent less than a cooldown ago is still valid, and the backend would not send
            // another one yet, so go straight back to it.
            if (state.resendInSeconds > 0) {
                _uiState.update { it.copy(step = ChangePasswordStep.OTP, errorMessage = null) }
                return
            }
            requestOtp(state)
        }

        private fun requestOtp(state: ChangePasswordUiState) =
            submit {
                when (val result = authRepository.requestChangePasswordOtp(state.currentPassword)) {
                    is AppResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                step = ChangePasswordStep.OTP,
                                otp = "",
                                noticeMessage =
                                    if (state.step == ChangePasswordStep.OTP) {
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

        private fun changePassword(state: ChangePasswordUiState) {
            AuthValidator.validateOtp(state.otp)?.let { error ->
                _uiState.update { it.copy(otpError = error) }
                return
            }
            submit {
                when (
                    val result =
                        authRepository.changePassword(
                            currentPassword = state.currentPassword,
                            newPassword = state.newPassword,
                            confirmPassword = state.confirmPassword,
                            otp = state.otp,
                        )
                ) {
                    is AppResult.Success ->
                        _uiState.update { it.copy(isSubmitting = false, isCompleted = true) }
                    is AppResult.Failure -> {
                        _uiState.update { it.copy(otp = "") }
                        fail(result.error)
                    }
                }
            }
        }

        private fun submit(block: suspend () -> Unit) {
            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null, noticeMessage = null)
            }
            viewModelScope.launch { block() }
        }

        private fun fail(error: AppError) {
            val code = (error as? AppError.Server)?.code
            _uiState.update {
                when {
                    error.isSessionEnded -> it.copy(isSubmitting = false, isSessionEnded = true)
                    // Both errors are about the passwords, so the user goes back to fix them.
                    code == INVALID_CREDENTIALS ->
                        it.copy(
                            isSubmitting = false,
                            step = ChangePasswordStep.PASSWORDS,
                            errorMessage = R.string.auth_error_current_password_incorrect,
                        )
                    code == PASSWORD_SAME_AS_OLD ->
                        it.copy(
                            isSubmitting = false,
                            step = ChangePasswordStep.PASSWORDS,
                            newPasswordError = FieldError.PasswordSameAsOld,
                        )
                    else -> it.copy(isSubmitting = false, errorMessage = error.messageRes())
                }
            }
        }

        // The access token could not be refreshed, so the local session is already gone.
        private val AppError.isSessionEnded: Boolean
            get() =
                this == AppError.Unauthorized ||
                    (this is AppError.Server && status == HTTP_UNAUTHORIZED)
    }
