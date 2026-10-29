package com.toeicspace.android.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        fun onSignUpClick() = completeOnboarding(OnboardingNextStep.SIGN_UP)

        fun onSignInClick() = completeOnboarding(OnboardingNextStep.SIGN_IN)

        fun onContinueAsGuestClick() = completeOnboarding(OnboardingNextStep.GUEST)

        fun onNavigationHandled() {
            _uiState.update { it.copy(nextStep = null) }
        }

        private fun completeOnboarding(nextStep: OnboardingNextStep) {
            val current = _uiState.value
            if (current.isCompleting || current.nextStep != null) return

            _uiState.update { it.copy(isCompleting = true) }
            viewModelScope.launch {
                // Persist first so the user never sees onboarding again after navigating away.
                sessionRepository.completeOnboarding()
                _uiState.update { it.copy(isCompleting = false, nextStep = nextStep) }
            }
        }
    }
