package com.toeicspace.android.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.toeicspace.android.feature.home.presentation.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

fun NavController.navigateToHome(navOptions: NavOptions? = null) =
    navigate(HomeDestination, navOptions)

fun NavGraphBuilder.homeScreen() {
    composable<HomeDestination> {
        HomeScreen()
    }
}
