package com.toeicspace.android.feature.course.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.course.presentation.detail.CourseDetailRoute
import kotlinx.serialization.Serializable

/** Full-screen course page above the shell in the root graph, so the tabs are hidden. */
@Serializable
data class CourseDetailDestination(
    val courseId: String,
) {
    companion object {
        /** The argument key, the property name above, for reading it from a SavedStateHandle. */
        const val ARG_COURSE_ID = "courseId"
    }
}

/** Tapping a card twice opens one copy, not a stack of them. */
private val SingleTop = navOptions { launchSingleTop = true }

fun NavController.navigateToCourseDetail(courseId: String) =
    navigate(CourseDetailDestination(courseId), SingleTop)

/**
 * After signing in: back to the course page the guest left to sign in, which then shows their
 * enrollment. False when sign in was not opened from a course page.
 */
fun NavController.returnToCourseAfterSignIn(): Boolean =
    popBackStack<CourseDetailDestination>(inclusive = false)

/**
 * [onSignInRequired] opens sign in for a guest who taps enroll or buy. [onOpenCheckout] opens the
 * checkout of a paid course; while it is null, buying shows "coming soon".
 */
fun NavGraphBuilder.courseDetailScreen(
    onBack: () -> Unit,
    onSignInRequired: () -> Unit,
    onOpenLesson: (courseId: String, lessonId: String, preview: Boolean) -> Unit,
    onOpenCheckout: ((courseId: String) -> Unit)? = null,
) {
    composable<CourseDetailDestination> {
        CourseDetailRoute(
            onBackClick = onBack,
            onSignInRequired = onSignInRequired,
            onOpenCheckout = onOpenCheckout,
            onOpenLesson = onOpenLesson,
        )
    }
}
