package com.toeicspace.android.feature.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.toeicspace.android.core.ui.components.BottomNavTab
import com.toeicspace.android.feature.main.presentation.MainRoute
import com.toeicspace.android.feature.main.presentation.Viewer
import kotlinx.serialization.Serializable

/** The shell screen in the root graph. Its tabs live in a nested [MainGraph]. */
@Serializable
data object MainDestination

@Serializable
data object MainGraph

@Serializable
data object HomeTabRoute

@Serializable
data object ExploreTabRoute

@Serializable
data object PracticeTabRoute

@Serializable
data object VocabularyTabRoute

@Serializable
data object ProfileTabRoute

fun NavController.navigateToMain(navOptions: NavOptions? = null) =
    navigate(MainDestination, navOptions)

fun NavGraphBuilder.mainScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onSessionEnded: () -> Unit,
) {
    composable<MainDestination> {
        MainRoute(
            onNavigateToSignIn = onNavigateToSignIn,
            onNavigateToSignUp = onNavigateToSignUp,
            onNavigateToAccountInfo = onNavigateToAccountInfo,
            onNavigateToChangePassword = onNavigateToChangePassword,
            onSessionEnded = onSessionEnded,
        )
    }
}

internal val BottomNavTab.route: Any
    get() =
        when (this) {
            BottomNavTab.HOME -> HomeTabRoute
            BottomNavTab.EXPLORE -> ExploreTabRoute
            BottomNavTab.PRACTICE -> PracticeTabRoute
            BottomNavTab.VOCABULARY -> VocabularyTabRoute
            BottomNavTab.PROFILE -> ProfileTabRoute
        }

/** Guests start on Explore, the only tab they can use without an account. */
internal val Viewer.startTab: BottomNavTab
    get() =
        when (this) {
            Viewer.Guest -> BottomNavTab.EXPLORE
            is Viewer.Member -> BottomNavTab.HOME
        }

internal fun NavDestination?.toTab(): BottomNavTab? =
    BottomNavTab.entries.firstOrNull { tab ->
        this?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
    }

/**
 * Switches tab and keeps one entry per tab: each tab's state is saved and restored, and back goes
 * to the start tab before leaving the app.
 */
internal fun NavController.navigateToTab(tab: BottomNavTab) =
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
