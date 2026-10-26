package com.toeicspace.android.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.toeicspace.android.core.util.ROUTE_AUTH
import com.toeicspace.android.core.util.ROUTE_HOME
import com.toeicspace.android.core.util.ROUTE_ONBOARDING
import com.toeicspace.android.core.util.ROUTE_SPLASH
import com.toeicspace.android.feature.onboarding.presentation.OnboardingNavTarget
import com.toeicspace.android.feature.onboarding.presentation.OnboardingRoute
import com.toeicspace.android.feature.splash.presentation.SplashNavTarget
import com.toeicspace.android.feature.splash.presentation.SplashRoute

@Composable
fun ToeicSpaceNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // 1. Màn hình Splash
        composable(route = ROUTE_SPLASH) {
            SplashRoute(
                onNavigate = { target ->
                    when (target) {
                        SplashNavTarget.ONBOARDING -> {
                            navController.navigate(ROUTE_ONBOARDING) {
                                popUpTo(ROUTE_SPLASH) { inclusive = true }
                            }
                        }

                        SplashNavTarget.AUTH -> {
                            navController.navigate(ROUTE_AUTH) {
                                popUpTo(ROUTE_SPLASH) { inclusive = true }
                            }
                        }

                        SplashNavTarget.HOME -> {
                            navController.navigate(ROUTE_HOME) {
                                popUpTo(ROUTE_SPLASH) { inclusive = true }
                            }
                        }
                    }
                },
            )
        }

        // 2. Màn hình Onboarding
        composable(route = ROUTE_ONBOARDING) {
            OnboardingRoute(
                onNavigate = { target ->
                    when (target) {
                        OnboardingNavTarget.REGISTER -> {
                            navController.navigate(ROUTE_AUTH) {
                                popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                            }
                        }

                        OnboardingNavTarget.LOGIN -> {
                            navController.navigate(ROUTE_AUTH) {
                                popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                            }
                        }

                        OnboardingNavTarget.HOME_GUEST -> {
                            navController.navigate(ROUTE_HOME) {
                                popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                            }
                        }
                    }
                },
            )
        }

        // 3. Màn hình Auth (Placeholder tạm thời)
        composable(route = ROUTE_AUTH) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🔐 Màn hình Đăng ký / Đăng nhập (Auth)")
            }
        }

        // 4. Màn hình Home (Placeholder tạm thời)
        composable(route = ROUTE_HOME) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🏠 Màn hình Chính (Home)")
            }
        }
    }
}
