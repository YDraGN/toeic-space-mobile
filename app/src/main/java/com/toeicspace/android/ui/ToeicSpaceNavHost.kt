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
import com.toeicspace.android.feature.main.navigation.mainScreen
import com.toeicspace.android.feature.main.navigation.navigateToMain
import com.toeicspace.android.feature.onboarding.navigation.OnboardingDestination
import com.toeicspace.android.feature.onboarding.navigation.navigateToOnboarding
import com.toeicspace.android.feature.onboarding.navigation.onboardingScreen
import com.toeicspace.android.feature.profile.navigation.accountInfoScreen
import com.toeicspace.android.feature.profile.navigation.navigateToAccountInfo
import com.toeicspace.android.feature.splash.navigation.SplashDestination
import com.toeicspace.android.feature.splash.navigation.splashScreen

/** Root navigation graph: Splash → Onboarding → Auth / Main shell (tabs) → Account info. */
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
            onNavigateToHome = { navController.navigateToMain(clearSplash) },
        )
        onboardingScreen(
            onNavigateToSignUp = { navController.navigateToSignUp(clearOnboarding) },
            onNavigateToSignIn = { navController.navigateToSignIn(navOptions = clearOnboarding) },
            onContinueAsGuest = { navController.navigateToMain(clearOnboarding) },
        )
        authScreens(
            navController = navController,
            onEnterApp = { navController.navigateToMain(clearAll()) },
        )
        mainScreen(
            onNavigateToSignIn = { navController.navigateToSignIn() },
            onNavigateToSignUp = { navController.navigateToSignUp() },
            onNavigateToAccountInfo = { navController.navigateToAccountInfo() },
            onSessionEnded = { navController.navigateToSignIn(navOptions = clearAll()) },
        )
        accountInfoScreen(onBack = { navController.popBackStack() })
    }
}
