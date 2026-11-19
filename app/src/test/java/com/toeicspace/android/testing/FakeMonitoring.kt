package com.toeicspace.android.testing

import com.toeicspace.android.core.analytics.AnalyticsEvent
import com.toeicspace.android.core.analytics.AnalyticsHelper
import com.toeicspace.android.core.crash.CrashReporter
import com.toeicspace.android.core.notifications.PushTokenProvider

/** Records what the app would send to Firebase Analytics. */
class FakeAnalyticsHelper : AnalyticsHelper {
    val events = mutableListOf<AnalyticsEvent>()
    val userIds = mutableListOf<String?>()

    fun named(name: String) = events.filter { it.name == name }

    override fun logEvent(event: AnalyticsEvent) {
        events += event
    }

    override fun setUserId(userId: String?) {
        userIds += userId
    }
}

/** Records what the app would send to Crashlytics. */
class FakeCrashReporter : CrashReporter {
    val logs = mutableListOf<String>()
    val recorded = mutableListOf<Throwable>()
    var lastUserId: String? = null
        private set

    override var isCollectionEnabled: Boolean = false
        private set

    override fun setUserId(userId: String?) {
        lastUserId = userId
    }

    override fun log(message: String) {
        logs += message
    }

    override fun recordException(throwable: Throwable) {
        recorded += throwable
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        isCollectionEnabled = enabled
    }
}

class FakePushTokenProvider(
    var token: String? = "fcm-token",
) : PushTokenProvider {
    override suspend fun token(): String? = token
}
