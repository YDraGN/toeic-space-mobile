package com.toeicspace.android.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Profile details for the Profile tab header (target score, level). Null for guests. */
@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        profileRepository: ProfileRepository,
    ) : ViewModel() {
        val profile: StateFlow<UserProfile?> =
            profileRepository.profile.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )
    }
