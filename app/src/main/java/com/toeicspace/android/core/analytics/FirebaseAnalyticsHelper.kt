package com.toeicspace.android.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseAnalyticsHelper
    @Inject
    constructor(
        private val analytics: FirebaseAnalytics,
    ) : AnalyticsHelper {
        override fun logEvent(event: AnalyticsEvent) {
            analytics.logEvent(event.name, event.params.toBundle())
        }

        override fun setUserId(userId: String?) {
            analytics.setUserId(userId)
        }
    }

private fun Map<String, Any>.toBundle(): Bundle =
    Bundle().also { bundle ->
        forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Long -> bundle.putLong(key, value)
                is Int -> bundle.putLong(key, value.toLong())
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putString(key, value.toString())
                else -> bundle.putString(key, value.toString())
            }
        }
    }
