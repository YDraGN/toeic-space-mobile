package com.toeicspace.android.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.toeicspace.android.feature.onboarding.presentation.OnboardingRoute
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingDestination

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) =
    navigate(OnboardingDestination, navOptions)

fun NavGraphBuilder.onboardingScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
) {
    composable<OnboardingDestination> {
        OnboardingRoute(
            onNavigateToSignUp = onNavigateToSignUp,
            onNavigateToSignIn = onNavigateToSignIn,
            onContinueAsGuest = onContinueAsGuest,
        )
    }
}
