package com.toeicspace.android.core.crash

import androidx.compose.runtime.staticCompositionLocalOf
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/** Crash reporting, behind an interface so view models and tests never touch Firebase. */
interface CrashReporter {
    /** The app's own user id, or null when signed out. Never the email or name. */
    fun setUserId(userId: String?)

    /** A breadcrumb shown with the next crash ("screen: CourseDetailDestination"). */
    fun log(message: String)

    /** An error the app recovered from, reported as a non-fatal. */
    fun recordException(throwable: Throwable)

    /** Debug builds start with collection off (see the manifest placeholder); this turns it on. */
    fun setCollectionEnabled(enabled: Boolean)

    val isCollectionEnabled: Boolean
}

object NoOpCrashReporter : CrashReporter {
    override fun setUserId(userId: String?) = Unit

    override fun log(message: String) = Unit

    override fun recordException(throwable: Throwable) = Unit

    override fun setCollectionEnabled(enabled: Boolean) = Unit

    override val isCollectionEnabled: Boolean = false
}

val LocalCrashReporter = staticCompositionLocalOf<CrashReporter> { NoOpCrashReporter }

@Singleton
internal class FirebaseCrashReporter
    @Inject
    constructor(
        private val crashlytics: FirebaseCrashlytics,
    ) : CrashReporter {
        override fun setUserId(userId: String?) {
            // Crashlytics has no "clear": an empty id stops tying later crashes to the last user.
            crashlytics.setUserId(userId.orEmpty())
        }

        override fun log(message: String) = crashlytics.log(message)

        override fun recordException(throwable: Throwable) = crashlytics.recordException(throwable)

        override fun setCollectionEnabled(enabled: Boolean) =
            crashlytics.setCrashlyticsCollectionEnabled(enabled)

        override val isCollectionEnabled: Boolean
            get() = crashlytics.isCrashlyticsCollectionEnabled
    }
