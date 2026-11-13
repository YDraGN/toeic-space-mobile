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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The form's fields, for live checks and the green "valid" border. */
enum class SignUpField {
    FULL_NAME,
    EMAIL,
    PHONE,
    PASSWORD,
    CONFIRM_PASSWORD,
}

/** Wait this long after the last keystroke before checking a field, so errors do not flash mid-word. */
internal const val LIVE_VALIDATION_DEBOUNCE_MILLIS = 400L

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
    /** Fields that passed their check; an optional field left empty is neither valid nor invalid. */
    val validFields: Set<SignUpField> = emptySet(),
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

        private val pendingChecks = mutableMapOf<SignUpField, Job>()

        // The shown error and border stay as they are while typing; the check after the pause
        // replaces them, so a mistake does not flash on and off with every key.
        fun onFullNameChange(value: String) = edit(SignUpField.FULL_NAME) { copy(fullName = value) }

        fun onEmailChange(value: String) = edit(SignUpField.EMAIL) { copy(email = value) }

        fun onPhoneChange(value: String) = edit(SignUpField.PHONE) { copy(phone = value) }

        fun onPasswordChange(value: String) {
            edit(SignUpField.PASSWORD) { copy(password = value) }
            // The confirmation is compared with the password, so it has to be checked again too.
            if (_uiState.value.confirmPassword.isNotEmpty()) {
                scheduleCheck(SignUpField.CONFIRM_PASSWORD)
            }
        }

        fun onConfirmPasswordChange(value: String) =
            edit(SignUpField.CONFIRM_PASSWORD) { copy(confirmPassword = value) }

        fun onSubmit() {
            val state = _uiState.value
            if (state.isSubmitting) return

            // Submitting checks everything now, without waiting for a pending check.
            pendingChecks.values.forEach(Job::cancel)
            pendingChecks.clear()
            val validated = SignUpField.entries.fold(state) { acc, field -> acc.checked(field) }
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

        private fun edit(
            field: SignUpField,
            transform: SignUpUiState.() -> SignUpUiState,
        ) {
            _uiState.update {
                it.transform().copy(
                    errorMessage = null,
                    serverRejectedFields = emptySet(),
                )
            }
            scheduleCheck(field)
        }

        private fun scheduleCheck(field: SignUpField) {
            pendingChecks[field]?.cancel()
            pendingChecks[field] =
                viewModelScope.launch {
                    delay(LIVE_VALIDATION_DEBOUNCE_MILLIS)
                    _uiState.update { it.checked(field) }
                }
        }

        /** Runs the field's rule and records the result as an error or as valid. */
        private fun SignUpUiState.checked(field: SignUpField): SignUpUiState {
            val error =
                when (field) {
                    SignUpField.FULL_NAME -> AuthValidator.validateFullName(fullName)
                    SignUpField.EMAIL -> AuthValidator.validateEmail(email)
                    SignUpField.PHONE -> AuthValidator.validatePhone(phone)
                    SignUpField.PASSWORD -> AuthValidator.validateNewAccountPassword(password)
                    SignUpField.CONFIRM_PASSWORD ->
                        AuthValidator.validateConfirmPassword(password, confirmPassword)
                }
            // Phone is optional: empty is fine but not worth a green border.
            val isValid = error == null && !(field == SignUpField.PHONE && phone.isBlank())
            val withError =
                when (field) {
                    SignUpField.FULL_NAME -> copy(fullNameError = error)
                    SignUpField.EMAIL -> copy(emailError = error)
                    SignUpField.PHONE -> copy(phoneError = error)
                    SignUpField.PASSWORD -> copy(passwordError = error)
                    SignUpField.CONFIRM_PASSWORD -> copy(confirmPasswordError = error)
                }
            return withError.copy(
                validFields = if (isValid) validFields + field else validFields - field,
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
