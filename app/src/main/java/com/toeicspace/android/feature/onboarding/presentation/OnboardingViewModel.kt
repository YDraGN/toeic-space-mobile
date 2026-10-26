package com.toeicspace.android.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingNavTarget {
    REGISTER,
    LOGIN,
    HOME_GUEST,
}

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        private val _navEvents = MutableSharedFlow<OnboardingNavTarget>()
        val navEvents: SharedFlow<OnboardingNavTarget> = _navEvents.asSharedFlow()

        fun onRegisterSelected() {
            completeAndNavigate(OnboardingNavTarget.REGISTER)
        }

        fun onLoginSelected() {
            completeAndNavigate(OnboardingNavTarget.LOGIN)
        }

        fun onGuestSelected() {
            completeAndNavigate(OnboardingNavTarget.HOME_GUEST)
        }

        private fun completeAndNavigate(target: OnboardingNavTarget) {
            viewModelScope.launch {
                userPreferences.setOnboardingCompleted(true)
                _navEvents.emit(target)
            }
        }
    }
