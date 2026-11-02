package com.toeicspace.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("pref_onboarding_completed")
private val KEY_GUEST_MODE = booleanPreferencesKey("pref_guest_mode")

data class UserPreferences(
    val isOnboardingCompleted: Boolean,
    val isGuest: Boolean,
)

/** Local, non-sensitive user preferences backed by Preferences DataStore. */
@Singleton
class UserPreferencesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val userPreferences: Flow<UserPreferences> =
            dataStore.data.map { prefs ->
                UserPreferences(
                    isOnboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false,
                    isGuest = prefs[KEY_GUEST_MODE] ?: false,
                )
            }

        suspend fun setOnboardingCompleted(completed: Boolean) {
            dataStore.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETED] = completed }
        }

        suspend fun setGuest(isGuest: Boolean) {
            dataStore.edit { prefs -> prefs[KEY_GUEST_MODE] = isGuest }
        }
    }
