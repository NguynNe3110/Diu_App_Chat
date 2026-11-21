package com.uzuu.diuchat.feature.onboarding.data.datasource.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.uzuu.diuchat.feature.onboarding.domain.model.OnboardingPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OnboardingPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val onboardingCompletedFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
        }

    val onboardingPreferencesFlow: Flow<OnboardingPreferences> =
        dataStore.data.map { preferences ->
            OnboardingPreferences(
                isCompleted = preferences[KEY_ONBOARDING_COMPLETED] ?: false,
                notificationsEnabled = preferences[KEY_NOTIFICATIONS_ENABLED] ?: false,
                microphoneEnabled = preferences[KEY_MICROPHONE_ENABLED] ?: false,
                quietHoursEnabled = preferences[KEY_QUIET_HOURS_ENABLED] ?: true,
            )
        }

    suspend fun saveOnboardingCompletion(
        notificationsEnabled: Boolean,
        microphoneEnabled: Boolean,
        quietHoursEnabled: Boolean,
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = true
            preferences[KEY_NOTIFICATIONS_ENABLED] = notificationsEnabled
            preferences[KEY_MICROPHONE_ENABLED] = microphoneEnabled
            preferences[KEY_QUIET_HOURS_ENABLED] = quietHoursEnabled
        }
    }

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("diu_onboarding_completed")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("diu_onboarding_notifications_enabled")
        val KEY_MICROPHONE_ENABLED = booleanPreferencesKey("diu_onboarding_microphone_enabled")
        val KEY_QUIET_HOURS_ENABLED = booleanPreferencesKey("diu_onboarding_quiet_hours_enabled")
    }
}
