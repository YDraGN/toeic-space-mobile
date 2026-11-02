package com.toeicspace.android.feature.splash.presentation

/** Screen the app should open after the splash. */
enum class StartDestination {
    ONBOARDING,
    SIGN_IN,
    HOME,
}

sealed interface SplashUiState {
    data object Loading : SplashUiState

    data class Ready(
        val startDestination: StartDestination,
    ) : SplashUiState
}
