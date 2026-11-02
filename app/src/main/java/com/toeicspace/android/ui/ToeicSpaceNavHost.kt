package com.toeicspace.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.toeicspace.android.feature.auth.navigation.authScreens
import com.toeicspace.android.feature.auth.navigation.navigateToSignIn
import com.toeicspace.android.feature.auth.navigation.navigateToSignUp
import com.toeicspace.android.feature.home.navigation.homeScreen
import com.toeicspace.android.feature.home.navigation.navigateToHome
import com.toeicspace.android.feature.onboarding.navigation.OnboardingDestination
import com.toeicspace.android.feature.onboarding.navigation.navigateToOnboarding
import com.toeicspace.android.feature.onboarding.navigation.onboardingScreen
import com.toeicspace.android.feature.splash.navigation.SplashDestination
import com.toeicspace.android.feature.splash.navigation.splashScreen

/** Root navigation graph: Splash → Onboarding → Auth / Home. */
@Composable
fun ToeicSpaceNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // Splash and onboarding are one-time screens, so they are removed from the back stack
    // once the user leaves them. Back from the next screen then exits the app.
    val clearSplash =
        navOptions {
            popUpTo<SplashDestination> { inclusive = true }
            launchSingleTop = true
        }
    val clearOnboarding =
        navOptions {
            popUpTo<OnboardingDestination> { inclusive = true }
            launchSingleTop = true
        }

    // Entering the app, or losing the session, starts a fresh back stack. Built on demand because
    // the graph id is only known once the NavHost has set the graph.
    fun clearAll() =
        navOptions {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }

    NavHost(
        navController = navController,
        startDestination = SplashDestination,
        modifier = modifier,
    ) {
        splashScreen(
            onNavigateToOnboarding = { navController.navigateToOnboarding(clearSplash) },
            onNavigateToSignIn = { navController.navigateToSignIn(navOptions = clearSplash) },
            onNavigateToHome = { navController.navigateToHome(clearSplash) },
        )
        onboardingScreen(
            onNavigateToSignUp = { navController.navigateToSignUp(clearOnboarding) },
            onNavigateToSignIn = { navController.navigateToSignIn(navOptions = clearOnboarding) },
            onContinueAsGuest = { navController.navigateToHome(clearOnboarding) },
        )
        authScreens(
            navController = navController,
            onEnterApp = { navController.navigateToHome(clearAll()) },
        )
        homeScreen(
            onNavigateToSignIn = { navController.navigateToSignIn() },
            onSessionEnded = { navController.navigateToSignIn(navOptions = clearAll()) },
        )
    }
}
