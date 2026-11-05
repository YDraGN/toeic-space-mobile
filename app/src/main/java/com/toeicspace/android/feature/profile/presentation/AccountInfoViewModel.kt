package com.toeicspace.android.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.Gender
import com.toeicspace.android.feature.profile.domain.ProfileFormValidator
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Every editable field at once: `PUT /users/me` replaces the whole profile, so it is sent whole. */
data class ProfileForm(
    val fullName: String,
    val phone: String,
    val dateOfBirth: LocalDate?,
    val gender: Gender?,
    val biography: String,
    val targetScore: Int?,
    val currentLevel: EnglishLevel?,
    val fullNameError: FieldError? = null,
    val phoneError: FieldError? = null,
    val biographyError: FieldError? = null,
)

data class AccountInfoUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    /** Non-null while the edit form is open. */
    val form: ProfileForm? = null,
    /** A valid form was submitted; the screen tells the user saving is not available yet. */
    val isSaveRequested: Boolean = false,
)

@HiltViewModel
class AccountInfoViewModel
    @Inject
    constructor(
        profileRepository: ProfileRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AccountInfoUiState())
        val uiState: StateFlow<AccountInfoUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                profileRepository.profile.collect { profile ->
                    _uiState.update { it.copy(isLoading = false, profile = profile) }
                }
            }
        }

        fun onEditClick() {
            val profile = _uiState.value.profile ?: return
            _uiState.update { it.copy(form = profile.toForm()) }
        }

        fun onCancelEdit() = _uiState.update { it.copy(form = null) }

        fun onFullNameChange(value: String) =
            editForm { copy(fullName = value, fullNameError = null) }

        fun onPhoneChange(value: String) = editForm { copy(phone = value, phoneError = null) }

        fun onDateOfBirthChange(value: LocalDate) = editForm { copy(dateOfBirth = value) }

        fun onGenderChange(value: Gender) = editForm { copy(gender = value) }

        fun onBiographyChange(value: String) =
            editForm { copy(biography = value, biographyError = null) }

        fun onTargetScoreChange(value: Int) = editForm { copy(targetScore = value) }

        fun onLevelChange(value: EnglishLevel) = editForm { copy(currentLevel = value) }

        fun onSaveClick() {
            val form = _uiState.value.form ?: return
            val checked =
                form.copy(
                    fullNameError = ProfileFormValidator.validateFullName(form.fullName),
                    phoneError = ProfileFormValidator.validatePhone(form.phone),
                    biographyError = ProfileFormValidator.validateBiography(form.biography),
                )
            val isValid =
                listOf(checked.fullNameError, checked.phoneError, checked.biographyError).all {
                    it ==
                        null
                }
            _uiState.update { it.copy(form = checked, isSaveRequested = isValid) }
        }

        fun onSaveHandled() = _uiState.update { it.copy(isSaveRequested = false) }

        private fun editForm(transform: ProfileForm.() -> ProfileForm) =
            _uiState.update { state -> state.copy(form = state.form?.transform()) }

        private fun UserProfile.toForm() =
            ProfileForm(
                fullName = fullName,
                phone = phone.orEmpty(),
                dateOfBirth = dateOfBirth,
                gender = gender,
                biography = biography.orEmpty(),
                targetScore = targetScore,
                currentLevel = currentLevel,
            )
    }
