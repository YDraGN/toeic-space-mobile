package com.toeicspace.android.core.analytics

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Product analytics, behind an interface so view models and tests never touch Firebase. Event and
 * parameter names follow Firebase's rules: lowercase, at most 40 characters, letters, digits and
 * underscores. Never pass personal data (names, emails) as a parameter.
 */
interface AnalyticsHelper {
    fun logEvent(event: AnalyticsEvent)

    /** The app's own user id, or null when signed out. Firebase never gets the email or name. */
    fun setUserId(userId: String?)
}

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
) {
    /** Names shared with the web where one exists, so both apps report the same funnel. */
    object Names {
        const val SCREEN_VIEW = "screen_view"
        const val LOGIN = "login"
        const val ENROLL_COURSE = "enroll_course"
        const val PURCHASE = "purchase"
    }

    object Params {
        const val SCREEN_NAME = "screen_name"
        const val METHOD = "method"
        const val COURSE_ID = "course_id"
        const val VALUE = "value"
        const val CURRENCY = "currency"
        const val TRANSACTION_ID = "transaction_id"
    }

    companion object {
        fun screenView(screenName: String) =
            AnalyticsEvent(Names.SCREEN_VIEW, mapOf(Params.SCREEN_NAME to screenName))

        fun login() = AnalyticsEvent(Names.LOGIN, mapOf(Params.METHOD to "email"))

        fun enrollCourse(courseId: String) =
            AnalyticsEvent(Names.ENROLL_COURSE, mapOf(Params.COURSE_ID to courseId))

        fun purchase(
            courseId: String,
            orderCode: String,
            amount: Long,
            currency: String,
        ) = AnalyticsEvent(
            Names.PURCHASE,
            mapOf(
                Params.COURSE_ID to courseId,
                Params.TRANSACTION_ID to orderCode,
                Params.VALUE to amount.toDouble(),
                Params.CURRENCY to currency,
            ),
        )
    }
}

/** Does nothing: the default in view model constructors, so tests need no fake unless they check events. */
object NoOpAnalyticsHelper : AnalyticsHelper {
    override fun logEvent(event: AnalyticsEvent) = Unit

    override fun setUserId(userId: String?) = Unit
}

/** For composables that log screen views; MainActivity provides the real one. */
val LocalAnalyticsHelper = staticCompositionLocalOf<AnalyticsHelper> { NoOpAnalyticsHelper }
