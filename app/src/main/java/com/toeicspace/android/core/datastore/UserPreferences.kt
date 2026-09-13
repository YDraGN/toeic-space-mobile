package com.toeicspace.android.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.toeicspace.android.core.util.PREF_AUTH_TOKEN
import com.toeicspace.android.core.util.PREF_ONBOARDING_COMPLETED
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "toeic_space_prefs",
)

@Singleton
class UserPreferences
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val keyOnboardingCompleted = booleanPreferencesKey(PREF_ONBOARDING_COMPLETED)
        private val keyAuthToken = stringPreferencesKey(PREF_AUTH_TOKEN)

        val isOnboardingCompleted: Flow<Boolean> =
            context.dataStore.data.map { prefs ->
                prefs[keyOnboardingCompleted] ?: false
            }

        val authToken: Flow<String?> =
            context.dataStore.data.map { prefs ->
                prefs[keyAuthToken]
            }

        suspend fun setOnboardingCompleted(completed: Boolean) {
            context.dataStore.edit { prefs ->
                prefs[keyOnboardingCompleted] = completed
            }
        }

        suspend fun saveAuthToken(token: String) {
            context.dataStore.edit { prefs ->
                prefs[keyAuthToken] = token
            }
        }

        suspend fun clearAuthToken() {
            context.dataStore.edit { prefs ->
                prefs.remove(keyAuthToken)
            }
        }
    }
