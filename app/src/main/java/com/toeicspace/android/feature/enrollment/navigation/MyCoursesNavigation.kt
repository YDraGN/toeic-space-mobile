package com.toeicspace.android.feature.enrollment.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.enrollment.presentation.MyCoursesRoute
import kotlinx.serialization.Serializable

/** "Khoá học của tôi", opened from the Profile tab; full screen above the shell. */
@Serializable
data object MyCoursesDestination

fun NavController.navigateToMyCourses() =
    navigate(MyCoursesDestination, navOptions { launchSingleTop = true })

fun NavGraphBuilder.myCoursesScreen(
    onBack: () -> Unit,
    onCourseClick: (courseId: String) -> Unit,
) {
    composable<MyCoursesDestination> {
        MyCoursesRoute(onBackClick = onBack, onCourseClick = onCourseClick)
    }
}
