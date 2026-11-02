package com.toeicspace.android.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.toeicspace.android.feature.home.presentation.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

fun NavController.navigateToHome(navOptions: NavOptions? = null) =
    navigate(HomeDestination, navOptions)

fun NavGraphBuilder.homeScreen(
    onNavigateToSignIn: () -> Unit,
    onSessionEnded: () -> Unit,
) {
    composable<HomeDestination> {
        HomeRoute(
            onNavigateToSignIn = onNavigateToSignIn,
            onSessionEnded = onSessionEnded,
        )
    }
}
