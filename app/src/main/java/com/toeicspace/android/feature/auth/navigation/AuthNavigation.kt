package com.toeicspace.android.feature.auth.navigation

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object SignUpDestination

@Serializable
data object SignInDestination

fun NavController.navigateToSignUp(navOptions: NavOptions? = null) =
    navigate(SignUpDestination, navOptions)

fun NavController.navigateToSignIn(navOptions: NavOptions? = null) =
    navigate(SignInDestination, navOptions)

// Placeholders until the Auth feature is implemented.
fun NavGraphBuilder.authScreens() {
    composable<SignUpDestination> {
        PlaceholderScreen(title = stringResource(id = R.string.auth_title_sign_up))
    }
    composable<SignInDestination> {
        PlaceholderScreen(title = stringResource(id = R.string.auth_title_sign_in))
    }
}
