package com.toeicspace.android.feature.splash.presentation

/** Screen the app should open after the splash. */
enum class StartDestination {
    ONBOARDING,
    HOME,
}

sealed interface SplashUiState {
    data object Loading : SplashUiState

    data object NetworkError : SplashUiState

    data class Ready(
        val startDestination: StartDestination,
    ) : SplashUiState
}
