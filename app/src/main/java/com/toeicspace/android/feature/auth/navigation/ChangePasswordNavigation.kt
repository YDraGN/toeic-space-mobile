package com.toeicspace.android.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.auth.presentation.changepassword.ChangePasswordRoute
import kotlinx.serialization.Serializable

/** Full-screen page above the shell in the root graph, opened from the Profile tab (members only). */
@Serializable
data object ChangePasswordDestination

fun NavController.navigateToChangePassword() =
    navigate(ChangePasswordDestination, navOptions { launchSingleTop = true })

/**
 * A new password ends every session of the account, this device's included, so [onPasswordChanged]
 * should restart at sign in. [onSessionEnded] is called when the session expired during the flow.
 */
fun NavGraphBuilder.changePasswordScreen(
    onBack: () -> Unit,
    onPasswordChanged: (email: String) -> Unit,
    onSessionEnded: () -> Unit,
) {
    composable<ChangePasswordDestination> {
        ChangePasswordRoute(
            onBackClick = onBack,
            onPasswordChanged = onPasswordChanged,
            onSessionEnded = onSessionEnded,
        )
    }
}
