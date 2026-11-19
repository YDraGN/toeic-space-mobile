package com.toeicspace.android.feature.vocabulary.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.presentation.review.ReviewSessionRoute
import kotlinx.serialization.Serializable

/** Full-screen review session above the shell. [source] is a [ReviewSource] name (DUE or MISTAKES). */
@Serializable
data class ReviewSessionDestination(
    val source: String,
) {
    companion object {
        const val ARG_SOURCE = "source"
    }
}

fun NavController.navigateToReviewSession(source: ReviewSource) =
    navigate(ReviewSessionDestination(source.name), navOptions { launchSingleTop = true })

fun NavGraphBuilder.reviewSessionScreen(onBack: () -> Unit) {
    composable<ReviewSessionDestination> { ReviewSessionRoute(onBackClick = onBack) }
}
