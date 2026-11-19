package com.toeicspace.android.core.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.toeicspace.android.core.crash.CrashReporter
import com.toeicspace.android.core.crash.LocalCrashReporter

/**
 * Logs a screen view, and leaves a Crashlytics breadcrumb, each time [navController] reaches a new
 * destination. Screens are named after their route class ("CourseDetailDestination"), never their
 * arguments, so no ids or emails reach analytics.
 */
@Composable
fun TrackScreenViews(
    navController: NavController,
    analytics: AnalyticsHelper = LocalAnalyticsHelper.current,
    crashReporter: CrashReporter = LocalCrashReporter.current,
) {
    DisposableEffect(navController, analytics, crashReporter) {
        val listener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                val name = destination.screenName() ?: return@OnDestinationChangedListener
                analytics.logEvent(AnalyticsEvent.screenView(name))
                crashReporter.log("screen: $name")
            }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }
}

/** "com.toeicspace.android.feature.course.navigation.CourseDetailDestination/{courseId}" -> "CourseDetailDestination". */
internal fun NavDestination.screenName(): String? = route?.let(::screenNameOf)

internal fun screenNameOf(route: String): String =
    route
        .substringBefore('/')
        .substringBefore('?')
        .substringAfterLast('.')

/** Provides both helpers to the composables under [content]. */
@Composable
fun ProvideMonitoring(
    analytics: AnalyticsHelper,
    crashReporter: CrashReporter,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAnalyticsHelper provides analytics,
        LocalCrashReporter provides crashReporter,
        content = content,
    )
}
