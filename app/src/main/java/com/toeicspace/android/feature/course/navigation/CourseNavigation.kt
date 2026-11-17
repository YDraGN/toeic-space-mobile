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

fun NavGraphBuilder.courseDetailScreen(onBack: () -> Unit) {
    composable<CourseDetailDestination> { CourseDetailRoute(onBackClick = onBack) }
}
