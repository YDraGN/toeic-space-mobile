package com.toeicspace.android.feature.auth.presentation.signup

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.auth.domain.AuthRepository
import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.auth.domain.RegistrationForm
import com.toeicspace.android.feature.auth.presentation.common.fieldMessage
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: FieldError? = null,
    val emailError: FieldError? = null,
    val phoneError: FieldError? = null,
    val passwordError: FieldError? = null,
    val confirmPasswordError: FieldError? = null,
    /** Fields the backend rejected for a rule the app does not check itself. */
    val serverRejectedFields: Set<String> = emptySet(),
    @param:StringRes val errorMessage: Int? = null,
    val isSubmitting: Boolean = false,
    val verification: PendingVerification? = null,
)

/** Account created; the user goes on to verify the email. */
data class PendingVerification(
    val email: String,
    val challengeId: String,
)

@HiltViewModel
class SignUpViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SignUpUiState())
        val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

        fun onFullNameChange(value: String) = edit { copy(fullName = value, fullNameError = null) }

        fun onEmailChange(value: String) = edit { copy(email = value, emailError = null) }

        fun onPhoneChange(value: String) = edit { copy(phone = value, phoneError = null) }

        fun onPasswordChange(value: String) = edit { copy(password = value, passwordError = null) }

        fun onConfirmPasswordChange(value: String) =
            edit {
                copy(confirmPassword = value, confirmPasswordError = null)
            }

        fun onSubmit() {
            val state = _uiState.value
            if (state.isSubmitting) return

            val validated =
                state.copy(
                    fullNameError = AuthValidator.validateFullName(state.fullName),
                    emailError = AuthValidator.validateEmail(state.email),
                    phoneError = AuthValidator.validatePhone(state.phone),
                    passwordError = AuthValidator.validateNewAccountPassword(state.password),
                    confirmPasswordError =
                        AuthValidator.validateConfirmPassword(
                            state.password,
                            state.confirmPassword,
                        ),
                )
            if (validated.hasFieldErrors) {
                _uiState.value = validated
                return
            }

            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null, serverRejectedFields = emptySet())
            }
            viewModelScope.launch {
                val form =
                    RegistrationForm(
                        fullName = state.fullName,
                        email = state.email,
                        phone = state.phone.ifBlank { null },
                        password = state.password,
                        confirmPassword = state.confirmPassword,
                    )
                when (val result = authRepository.register(form)) {
                    is AppResult.Success ->
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                verification =
                                    PendingVerification(
                                        AuthValidator.normalizeEmail(state.email),
                                        result.data,
                                    ),
                            )
                        }
                    is AppResult.Failure -> _uiState.update { it.withError(result.error) }
                }
            }
        }

        fun onNavigationHandled() = _uiState.update { it.copy(verification = null) }

        private fun edit(transform: SignUpUiState.() -> SignUpUiState) =
            _uiState.update {
                it.transform().copy(
                    errorMessage = null,
                    serverRejectedFields = emptySet(),
                )
            }

        private fun SignUpUiState.withError(error: AppError): SignUpUiState {
            val fields =
                listOf("fullName", "email", "phone", "password", "confirmPassword")
                    .filter { field -> error.fieldMessage(field) != null }
                    .toSet()
            return copy(
                isSubmitting = false,
                serverRejectedFields = fields,
                errorMessage = if (fields.isEmpty()) error.messageRes() else null,
            )
        }

        private val SignUpUiState.hasFieldErrors: Boolean
            get() =
                listOf(fullNameError, emailError, phoneError, passwordError, confirmPasswordError)
                    .any { it != null }
    }
