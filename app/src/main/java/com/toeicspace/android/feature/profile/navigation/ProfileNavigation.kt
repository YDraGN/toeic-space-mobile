package com.toeicspace.android.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.profile.presentation.AccountInfoRoute
import kotlinx.serialization.Serializable

/** Full-screen account details above the shell in the root graph, so the tabs are hidden. */
@Serializable
data object AccountInfoDestination

/** Tapping twice opens one copy, not a stack of them. */
private val SingleTop = navOptions { launchSingleTop = true }

fun NavController.navigateToAccountInfo() = navigate(AccountInfoDestination, SingleTop)

fun NavGraphBuilder.accountInfoScreen(onBack: () -> Unit) {
    composable<AccountInfoDestination> { AccountInfoRoute(onBackClick = onBack) }
}
