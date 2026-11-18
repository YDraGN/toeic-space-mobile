package com.toeicspace.android.feature.classes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.classes.presentation.detail.MyClassDetailRoute
import com.toeicspace.android.feature.classes.presentation.list.MyClassesRoute
import kotlinx.serialization.Serializable

/** The student's classes, full screen above the shell in the root graph. */
@Serializable
data object MyClassesDestination

@Serializable
data class MyClassDetailDestination(
    val classId: String,
) {
    companion object {
        /** The argument key, the property name above, for reading it from a SavedStateHandle. */
        const val ARG_CLASS_ID = "classId"
    }
}

/** Tapping twice opens one copy, not a stack of them. */
private val SingleTop = navOptions { launchSingleTop = true }

fun NavController.navigateToMyClasses() = navigate(MyClassesDestination, SingleTop)

fun NavController.navigateToMyClassDetail(classId: String) =
    navigate(MyClassDetailDestination(classId), SingleTop)

fun NavGraphBuilder.myClassesScreens(
    onBack: () -> Unit,
    onClassClick: (classId: String) -> Unit,
) {
    composable<MyClassesDestination> {
        MyClassesRoute(onBackClick = onBack, onClassClick = onClassClick)
    }
    composable<MyClassDetailDestination> { MyClassDetailRoute(onBackClick = onBack) }
}
