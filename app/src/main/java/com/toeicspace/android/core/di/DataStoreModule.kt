package com.toeicspace.android.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.toeicspace.android.core.datastore.SessionDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Keep the existing file name so installed apps do not lose their onboarding flag.
private const val USER_PREFERENCES_FILE = "toeic_space_prefs"

// Encrypted session (refresh token, cached user). Like every DataStore file, excluded from backups.
private const val SESSION_FILE = "toeic_space_session"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(USER_PREFERENCES_FILE) },
        )

    @Provides
    @Singleton
    @SessionDataStore
    fun provideSessionDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(SESSION_FILE) },
        )
}
