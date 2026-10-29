package com.toeicspace.android.feature.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.toeicspace.android.feature.splash.presentation.SplashRoute
import kotlinx.serialization.Serializable

@Serializable
data object SplashDestination

fun NavGraphBuilder.splashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    composable<SplashDestination> {
        SplashRoute(
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToHome = onNavigateToHome,
        )
    }
}
