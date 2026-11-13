package com.toeicspace.android.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Profile details for the Profile tab header (target score, level). Null for guests. */
@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
    ) : ViewModel() {
        val profile: StateFlow<UserProfile?> =
            profileRepository.profile.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

        val isUploadingAvatar: StateFlow<Boolean> =
            profileRepository.isUploadingAvatar.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

        private val _message = MutableStateFlow<ProfileMessage?>(null)

        /** Result of an avatar upload started from the tab; cleared with [onMessageShown]. */
        val message: StateFlow<ProfileMessage?> = _message.asStateFlow()

        init {
            // Target score and level only come from the API; offline the header shows name and email.
            viewModelScope.launch { profileRepository.refresh() }
        }

        /** [imageUri] is the content URI the photo picker returned. */
        fun onAvatarPicked(imageUri: String) {
            if (isUploadingAvatar.value) return
            viewModelScope.launch {
                _message.value =
                    when (val result = profileRepository.updateAvatar(imageUri)) {
                        is AppResult.Success -> ProfileMessage.AvatarSaved
                        is AppResult.Failure -> ProfileMessage.AvatarFailed(result.error)
                    }
            }
        }

        fun onMessageShown() {
            _message.value = null
        }
    }
