package com.toeicspace.android.feature.onboarding.presentation

/** Where the user goes after leaving onboarding. */
enum class OnboardingNextStep {
    SIGN_UP,
    SIGN_IN,
    GUEST,
}

data class OnboardingUiState(
    val isCompleting: Boolean = false,
    // Set once the onboarding flag is persisted; the route navigates and then clears it.
    val nextStep: OnboardingNextStep? = null,
)
