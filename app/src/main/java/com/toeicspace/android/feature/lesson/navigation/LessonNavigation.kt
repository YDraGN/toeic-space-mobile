package com.toeicspace.android.feature.lesson.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.lesson.presentation.LessonRoute
import kotlinx.serialization.Serializable

/**
 * The lesson player, full screen above the shell. [preview] opens a free-preview lesson for someone
 * not enrolled; "Bài trước" / "Bài sau" then only move between free previews.
 */
@Serializable
data class LessonDestination(
    val courseId: String,
    val lessonId: String,
    val preview: Boolean = false,
) {
    companion object {
        /** Argument keys, the property names above, for reading them from a SavedStateHandle. */
        const val ARG_COURSE_ID = "courseId"
        const val ARG_LESSON_ID = "lessonId"
        const val ARG_PREVIEW = "preview"
    }
}

fun NavController.navigateToLesson(
    courseId: String,
    lessonId: String,
    preview: Boolean,
) = navigate(LessonDestination(courseId, lessonId, preview), navOptions { launchSingleTop = true })

fun NavGraphBuilder.lessonScreen(onBack: () -> Unit) {
    composable<LessonDestination> { LessonRoute(onBackClick = onBack) }
}
