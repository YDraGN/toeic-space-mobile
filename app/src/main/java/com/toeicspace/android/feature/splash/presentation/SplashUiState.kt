package com.toeicspace.android.feature.splash.presentation

enum class SplashNavTarget {
    ONBOARDING,
    AUTH,
    HOME,
}

sealed interface SplashUiState {
    data object Loading : SplashUiState

    data class Success(
        val target: SplashNavTarget,
    ) : SplashUiState

    data class NetworkError(
        val message: String? = null,
    ) : SplashUiState
}
