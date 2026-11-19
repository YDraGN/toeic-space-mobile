package com.toeicspace.android.core.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.toeicspace.android.core.analytics.AnalyticsHelper
import com.toeicspace.android.core.analytics.FirebaseAnalyticsHelper
import com.toeicspace.android.core.crash.CrashReporter
import com.toeicspace.android.core.crash.FirebaseCrashReporter
import com.toeicspace.android.core.notifications.FirebasePushTokenProvider
import com.toeicspace.android.core.notifications.PushTokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Firebase (PBL-110): Analytics, Crashlytics and Cloud Messaging behind app interfaces. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class FirebaseModule {
    @Binds
    abstract fun bindAnalyticsHelper(impl: FirebaseAnalyticsHelper): AnalyticsHelper

    @Binds
    abstract fun bindCrashReporter(impl: FirebaseCrashReporter): CrashReporter

    @Binds
    abstract fun bindPushTokenProvider(impl: FirebasePushTokenProvider): PushTokenProvider

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAnalytics(
            @ApplicationContext context: Context,
        ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

        @Provides
        @Singleton
        fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
    }
}
