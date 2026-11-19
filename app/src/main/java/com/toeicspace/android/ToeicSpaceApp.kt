package com.toeicspace.android

import android.app.Application
import com.toeicspace.android.core.analytics.AnalyticsHelper
import com.toeicspace.android.core.crash.CrashReporter
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.di.ApplicationScope
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.notifications.AppNotifications
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ToeicSpaceApp : Application() {
    @Inject lateinit var sessionRepository: SessionRepository

    @Inject lateinit var analytics: AnalyticsHelper

    @Inject lateinit var crashReporter: CrashReporter

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        AppNotifications.createChannels(this)
        // Crashes and analytics carry the signed-in user's id only, never their email or name.
        applicationScope.launch {
            sessionRepository.session
                .filterIsInstance<SessionState.Ready>()
                .map { it.user?.id }
                .distinctUntilChanged()
                .collect { userId ->
                    analytics.setUserId(userId)
                    crashReporter.setUserId(userId)
                }
        }
    }
}
