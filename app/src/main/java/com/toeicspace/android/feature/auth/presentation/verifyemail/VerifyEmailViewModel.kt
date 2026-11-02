package com.toeicspace.android.feature.auth.presentation.verifyemail

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.auth.domain.AuthRepository
import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.auth.presentation.common.DEFAULT_RESEND_COOLDOWN_SECONDS
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

data class VerifyEmailUiState(
    val email: String,
    val challengeId: String,
    val otp: String = "",
    val otpError: FieldError? = null,
    @param:StringRes val errorMessage: Int? = null,
    @param:StringRes val noticeMessage: Int? = null,
    val resendInSeconds: Int = 0,
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val isVerified: Boolean = false,
) {
    val canResend: Boolean get() = resendInSeconds == 0 && !isResending && !isSubmitting
}

@HiltViewModel(assistedFactory = VerifyEmailViewModel.Factory::class)
class VerifyEmailViewModel
    @AssistedInject
    constructor(
        @Assisted("email") email: String,
        @Assisted("challengeId") challengeId: String,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted("email") email: String,
                @Assisted("challengeId") challengeId: String,
            ): VerifyEmailViewModel
        }

        private val _uiState =
            MutableStateFlow(VerifyEmailUiState(email = email, challengeId = challengeId))
        val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

        private val countdown =
            ResendCountdown(viewModelScope) { seconds ->
                _uiState.update {
                    it.copy(resendInSeconds = seconds)
                }
            }

        init {
            // An OTP was sent just before this screen opened.
            countdown.start(DEFAULT_RESEND_COOLDOWN_SECONDS)
        }

        fun onOtpChange(value: String) {
            val digits = value.filter(Char::isDigit).take(AuthValidator.OTP_LENGTH)
            _uiState.update { it.copy(otp = digits, otpError = null, errorMessage = null) }
            // Submit as soon as the last digit is typed.
            if (digits.length == AuthValidator.OTP_LENGTH) onSubmit()
        }

        fun onSubmit() {
            val state = _uiState.value
            if (state.isSubmitting || state.isVerified) return
            AuthValidator.validateOtp(state.otp)?.let { error ->
                _uiState.update { it.copy(otpError = error) }
                return
            }

            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    noticeMessage = null,
                )
            }
            viewModelScope.launch {
                when (val result = authRepository.verifyEmail(state.challengeId, state.otp)) {
                    is AppResult.Success ->
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                isVerified = true,
                            )
                        }
                    is AppResult.Failure ->
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                otp = "",
                                errorMessage = result.error.messageRes(),
                            )
                        }
                }
            }
        }

        fun onResendClick() {
            val state = _uiState.value
            if (!state.canResend) return

            _uiState.update {
                it.copy(
                    isResending = true,
                    errorMessage = null,
                    noticeMessage = null,
                )
            }
            viewModelScope.launch {
                when (val result = authRepository.resendVerification(state.email)) {
                    is AppResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isResending = false,
                                challengeId = result.data,
                                otp = "",
                                noticeMessage = R.string.auth_notice_otp_resent,
                            )
                        }
                        countdown.start(DEFAULT_RESEND_COOLDOWN_SECONDS)
                    }
                    is AppResult.Failure ->
                        _uiState.update {
                            it.copy(
                                isResending = false,
                                errorMessage = result.error.messageRes(),
                            )
                        }
                }
            }
        }

        fun onNavigationHandled() = _uiState.update { it.copy(isVerified = false) }
    }
