package com.toeicspace.android.feature.auth.presentation.signin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.feature.auth.domain.AuthRepository
import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.auth.presentation.common.isEmailNotVerified
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

enum class SignInNotice {
    EMAIL_VERIFIED,
    PASSWORD_RESET,
}

sealed interface SignInNextStep {
    data object Home : SignInNextStep

    data class VerifyEmail(
        val email: String,
        val challengeId: String,
    ) : SignInNextStep
}

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    @param:StringRes val errorMessage: Int? = null,
    @param:StringRes val noticeMessage: Int? = null,
    val isSubmitting: Boolean = false,
    val nextStep: SignInNextStep? = null,
)

@HiltViewModel(assistedFactory = SignInViewModel.Factory::class)
class SignInViewModel
    @AssistedInject
    constructor(
        @Assisted initialEmail: String,
        @Assisted notice: SignInNotice?,
        private val authRepository: AuthRepository,
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(
                initialEmail: String,
                notice: SignInNotice?,
            ): SignInViewModel
        }

        private val _uiState =
            MutableStateFlow(
                SignInUiState(
                    email = initialEmail,
                    noticeMessage =
                        when (notice) {
                            SignInNotice.EMAIL_VERIFIED -> R.string.auth_notice_email_verified
                            SignInNotice.PASSWORD_RESET -> R.string.auth_notice_password_reset
                            null -> null
                        },
                ),
            )
        val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

        fun onEmailChange(value: String) =
            _uiState.update {
                it.copy(email = value, emailError = null, errorMessage = null)
            }

        fun onPasswordChange(value: String) =
            _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }

        fun onSubmit() {
            val state = _uiState.value
            if (state.isSubmitting) return

            val emailError = AuthValidator.validateEmail(state.email)
            val passwordError = AuthValidator.validateSignInPassword(state.password)
            if (emailError != null || passwordError != null) {
                _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
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
                when (val result = authRepository.signIn(state.email, state.password)) {
                    is AppResult.Success ->
                        _uiState.update {
                            it.copy(isSubmitting = false, nextStep = SignInNextStep.Home)
                        }
                    is AppResult.Failure ->
                        if (result.error.isEmailNotVerified) {
                            startEmailVerification(state.email)
                        } else {
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    errorMessage = result.error.messageRes(),
                                )
                            }
                        }
                }
            }
        }

        fun onContinueAsGuestClick() {
            if (_uiState.value.isSubmitting) return
            _uiState.update { it.copy(isSubmitting = true) }
            viewModelScope.launch {
                sessionRepository.continueAsGuest()
                _uiState.update { it.copy(isSubmitting = false, nextStep = SignInNextStep.Home) }
            }
        }

        fun onNavigationHandled() = _uiState.update { it.copy(nextStep = null) }

        // The password was correct but the email is unverified: send a fresh OTP and go verify it.
        private suspend fun startEmailVerification(email: String) {
            when (val result = authRepository.resendVerification(email)) {
                is AppResult.Success ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            nextStep =
                                SignInNextStep.VerifyEmail(
                                    AuthValidator.normalizeEmail(email),
                                    result.data,
                                ),
                        )
                    }
                is AppResult.Failure ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.error.messageRes(),
                        )
                    }
            }
        }
    }
