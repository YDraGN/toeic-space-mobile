package com.toeicspace.android.feature.checkout.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.checkout.presentation.CheckoutRoute
import kotlinx.serialization.Serializable

/**
 * Buying one paid course, full screen above the shell. Only for signed-in users: the caller sends
 * guests to sign in instead of here.
 */
@Serializable
data class CheckoutDestination(
    val courseId: String,
) {
    companion object {
        /** The argument key, the property name above, for reading it from a SavedStateHandle. */
        const val ARG_COURSE_ID = "courseId"
    }
}

/** A double tap on the buy button opens one checkout, not two. */
private val SingleTop = navOptions { launchSingleTop = true }

fun NavController.navigateToCheckout(courseId: String) =
    navigate(CheckoutDestination(courseId), SingleTop)

/** [onPaid] runs when the learner leaves the success state to start learning [courseId]. */
fun NavGraphBuilder.checkoutScreen(
    onBack: () -> Unit,
    onPaid: (courseId: String) -> Unit,
) {
    composable<CheckoutDestination> { CheckoutRoute(onBackClick = onBack, onPaid = onPaid) }
}
