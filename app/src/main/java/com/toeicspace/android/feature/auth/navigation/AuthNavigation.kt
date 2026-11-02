package com.toeicspace.android.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.toeicspace.android.feature.auth.presentation.forgotpassword.ForgotPasswordRoute
import com.toeicspace.android.feature.auth.presentation.signin.SignInNotice
import com.toeicspace.android.feature.auth.presentation.signin.SignInRoute
import com.toeicspace.android.feature.auth.presentation.signup.SignUpRoute
import com.toeicspace.android.feature.auth.presentation.verifyemail.VerifyEmailRoute
import kotlinx.serialization.Serializable

@Serializable
data class SignInDestination(
    val email: String = "",
    val showEmailVerified: Boolean = false,
    val showPasswordReset: Boolean = false,
)

@Serializable
data object SignUpDestination

@Serializable
data class VerifyEmailDestination(
    val email: String,
    val challengeId: String,
)

@Serializable
data class ForgotPasswordDestination(
    val email: String = "",
)

fun NavController.navigateToSignIn(
    destination: SignInDestination = SignInDestination(),
    navOptions: NavOptions? = null,
) = navigate(destination, navOptions)

fun NavController.navigateToSignUp(navOptions: NavOptions? = null) =
    navigate(SignUpDestination, navOptions)

/**
 * Sign in, sign up, email verification and password reset.
 * [onEnterApp] is called once the user is signed in or chose guest mode.
 */
fun NavGraphBuilder.authScreens(
    navController: NavController,
    onEnterApp: () -> Unit,
) {
    // After verifying an email or resetting a password the user signs in from a clean stack.
    val restartAtSignIn: (SignInDestination) -> Unit = { destination ->
        navController.navigate(destination) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    composable<SignInDestination> { entry ->
        val route = entry.toRoute<SignInDestination>()
        SignInRoute(
            initialEmail = route.email,
            notice =
                when {
                    route.showEmailVerified -> SignInNotice.EMAIL_VERIFIED
                    route.showPasswordReset -> SignInNotice.PASSWORD_RESET
                    else -> null
                },
            onSignedIn = onEnterApp,
            onNavigateToVerifyEmail = { email, challengeId ->
                navController.navigate(VerifyEmailDestination(email, challengeId))
            },
            onNavigateToSignUp = { navController.navigateToSignUp() },
            onNavigateToForgotPassword = { email ->
                navController.navigate(ForgotPasswordDestination(email))
            },
            onBackClick =
                if (navController.previousBackStackEntry != null) {
                    { navController.popBackStack() }
                } else {
                    null
                },
        )
    }
    composable<SignUpDestination> {
        SignUpRoute(
            onBackClick = { navController.popBackStack() },
            onNavigateToVerifyEmail = { email, challengeId ->
                navController.navigate(VerifyEmailDestination(email, challengeId))
            },
            onNavigateToSignIn = {
                navController.navigateToSignIn(
                    navOptions =
                        NavOptions
                            .Builder()
                            .setPopUpTo<SignUpDestination>(inclusive = true)
                            .setLaunchSingleTop(true)
                            .build(),
                )
            },
        )
    }
    composable<VerifyEmailDestination> { entry ->
        val route = entry.toRoute<VerifyEmailDestination>()
        VerifyEmailRoute(
            email = route.email,
            challengeId = route.challengeId,
            onBackClick = { navController.popBackStack() },
            onVerified = { email ->
                restartAtSignIn(SignInDestination(email = email, showEmailVerified = true))
            },
        )
    }
    composable<ForgotPasswordDestination> { entry ->
        val route = entry.toRoute<ForgotPasswordDestination>()
        ForgotPasswordRoute(
            initialEmail = route.email,
            onBackClick = { navController.popBackStack() },
            onPasswordReset = { email ->
                restartAtSignIn(SignInDestination(email = email, showPasswordReset = true))
            },
        )
    }
}
