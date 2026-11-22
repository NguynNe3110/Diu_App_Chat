package com.uzuu.diuchat.feature.settings.data.datasource.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val themeModeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: DEFAULT_THEME_MODE
    }

    val readReceiptsFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_READ_RECEIPTS_ENABLED] ?: DEFAULT_READ_RECEIPTS_ENABLED
    }

    val softSendAnimationFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_SOFT_SEND_ANIMATION_ENABLED] ?: DEFAULT_SOFT_SEND_ANIMATION_ENABLED
    }

    val notificationPrivacyFlow: Flow<NotificationPrivacy> = dataStore.data.map { preferences ->
        val raw = preferences[KEY_NOTIFICATION_PRIVACY] ?: NotificationPrivacy.FULL_CONTENT.name
        runCatching { NotificationPrivacy.valueOf(raw) }.getOrDefault(NotificationPrivacy.FULL_CONTENT)
    }

    val notificationSoundFlow: Flow<NotificationSound> = dataStore.data.map { preferences ->
        val raw = preferences[KEY_NOTIFICATION_SOUND] ?: NotificationSound.WATER_DROP.name
        runCatching { NotificationSound.valueOf(raw) }.getOrDefault(NotificationSound.WATER_DROP)
    }

    val quietScheduleFlow: Flow<QuietSchedule> = dataStore.data.map { preferences ->
        val repeatDays = preferences[KEY_QUIET_HOURS_REPEAT_DAYS]
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: QuietSchedule.DEFAULT_REPEAT_DAYS
        QuietSchedule(
            enabled = preferences[KEY_QUIET_HOURS_ENABLED] ?: true,
            startMinuteOfDay = preferences[KEY_QUIET_HOURS_START_MINUTE] ?: (22 * 60 + 30),
            endMinuteOfDay = preferences[KEY_QUIET_HOURS_END_MINUTE] ?: (6 * 60),
            timeZoneId = preferences[KEY_QUIET_HOURS_TIMEZONE] ?: "Asia/Ho_Chi_Minh",
            allowedEmergencyContactIds = preferences[KEY_QUIET_HOURS_ALLOWED_CONTACTS]
                ?: setOf("emergency_contact_linh"),
            repeatDays = repeatDays,
        )
    }

    val userProfileFlow: Flow<UserProfile> = dataStore.data.map { preferences ->
        UserProfile(
            id = preferences[KEY_USER_ID] ?: "",
            displayName = preferences[KEY_USER_DISPLAY_NAME] ?: "",
            username = preferences[KEY_USER_USERNAME] ?: "",
            bio = preferences[KEY_USER_BIO] ?: "",
            birthday = preferences[KEY_USER_BIRTHDAY] ?: "",
            phone = preferences[KEY_USER_PHONE] ?: "",
            primaryMood = preferences[KEY_USER_PRIMARY_MOOD] ?: "Bình yên",
            avatarUrl = preferences[KEY_USER_AVATAR_URL],
        )
    }

    val blockedContactsFlow: Flow<List<BlockedContact>> = dataStore.data.map { preferences ->
        val raw = preferences[KEY_BLOCKED_CONTACTS_CSV]
        if (raw.isNullOrBlank()) {
            defaultBlockedContacts()
        } else {
            raw.split(";").mapNotNull { entry ->
                val tokens = entry.split("|")
                if (tokens.size >= 4) {
                    BlockedContact(
                        id = tokens[0],
                        name = tokens[1],
                        phone = tokens[2],
                        blockedAt = tokens[3],
                    )
                } else {
                    null
                }
            }
        }
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode
        }
    }

    suspend fun updateReadReceipts(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_READ_RECEIPTS_ENABLED] = enabled
        }
    }

    suspend fun updateSoftSendAnimation(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SOFT_SEND_ANIMATION_ENABLED] = enabled
        }
    }

    suspend fun updateNotificationPrivacy(privacy: NotificationPrivacy) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_PRIVACY] = privacy.name
        }
    }

    suspend fun updateNotificationSound(sound: NotificationSound) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_SOUND] = sound.name
        }
    }

    suspend fun updateQuietSchedule(schedule: QuietSchedule) {
        dataStore.edit { preferences ->
            preferences[KEY_QUIET_HOURS_ENABLED] = schedule.enabled
            preferences[KEY_QUIET_HOURS_START_MINUTE] = schedule.startMinuteOfDay
            preferences[KEY_QUIET_HOURS_END_MINUTE] = schedule.endMinuteOfDay
            preferences[KEY_QUIET_HOURS_TIMEZONE] = schedule.timeZoneId
            preferences[KEY_QUIET_HOURS_ALLOWED_CONTACTS] = schedule.allowedEmergencyContactIds
            preferences[KEY_QUIET_HOURS_REPEAT_DAYS] = schedule.repeatDays.map { it.toString() }.toSet()
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = profile.id
            preferences[KEY_USER_DISPLAY_NAME] = profile.displayName
            preferences[KEY_USER_USERNAME] = profile.username
            preferences[KEY_USER_BIO] = profile.bio
            preferences[KEY_USER_BIRTHDAY] = profile.birthday
            preferences[KEY_USER_PHONE] = profile.phone
            preferences[KEY_USER_PRIMARY_MOOD] = profile.primaryMood
            if (profile.avatarUrl != null) {
                preferences[KEY_USER_AVATAR_URL] = profile.avatarUrl
            } else {
                preferences.remove(KEY_USER_AVATAR_URL)
            }
        }
    }

    suspend fun updateBlockedContacts(contacts: List<BlockedContact>) {
        dataStore.edit { preferences ->
            preferences[KEY_BLOCKED_CONTACTS_CSV] = contacts.joinToString(";") {
                "${it.id}|${it.name}|${it.phone}|${it.blockedAt}"
            }
        }
    }

    companion object {
        private const val DEFAULT_THEME_MODE = "SYSTEM"
        private const val DEFAULT_READ_RECEIPTS_ENABLED = true
        private const val DEFAULT_SOFT_SEND_ANIMATION_ENABLED = true

        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("diu_onboarding_completed")
        val KEY_THEME_MODE = stringPreferencesKey("diu_theme_mode")
        val KEY_READ_RECEIPTS_ENABLED = booleanPreferencesKey("diu_read_receipts_enabled")
        val KEY_SOFT_SEND_ANIMATION_ENABLED = booleanPreferencesKey("diu_soft_send_animation_enabled")
        val KEY_NOTIFICATION_PRIVACY = stringPreferencesKey("diu_notification_privacy")
        val KEY_NOTIFICATION_SOUND = stringPreferencesKey("diu_notification_sound")
        val KEY_QUIET_HOURS_ENABLED = booleanPreferencesKey("diu_quiet_hours_enabled")
        val KEY_QUIET_HOURS_START_MINUTE = intPreferencesKey("diu_quiet_hours_start_minute")
        val KEY_QUIET_HOURS_END_MINUTE = intPreferencesKey("diu_quiet_hours_end_minute")
        val KEY_QUIET_HOURS_TIMEZONE = stringPreferencesKey("diu_quiet_hours_timezone")
        val KEY_QUIET_HOURS_ALLOWED_CONTACTS = stringSetPreferencesKey("diu_quiet_hours_allowed_contacts")
        val KEY_QUIET_HOURS_REPEAT_DAYS = stringSetPreferencesKey("diu_quiet_hours_repeat_days")

        val KEY_USER_ID = stringPreferencesKey("diu_user_id")
        val KEY_USER_DISPLAY_NAME = stringPreferencesKey("diu_user_display_name")
        val KEY_USER_USERNAME = stringPreferencesKey("diu_user_username")
        val KEY_USER_BIO = stringPreferencesKey("diu_user_bio")
        val KEY_USER_BIRTHDAY = stringPreferencesKey("diu_user_birthday")
        val KEY_USER_PHONE = stringPreferencesKey("diu_user_phone")
        val KEY_USER_PRIMARY_MOOD = stringPreferencesKey("diu_user_primary_mood")
        val KEY_USER_AVATAR_URL = stringPreferencesKey("diu_user_avatar_url")

        val KEY_BLOCKED_CONTACTS_CSV = stringPreferencesKey("diu_blocked_contacts_csv")

        fun defaultBlockedContacts(): List<BlockedContact> = listOf(
            BlockedContact(
                id = "block_1",
                name = "Quảng cáo Sim Số Đẹp",
                phone = "+84900112233",
                blockedAt = "15/11/2026",
            ),
            BlockedContact(
                id = "block_2",
                name = "Người lạ nhắn tin rác",
                phone = "+84911223344",
                blockedAt = "18/11/2026",
            ),
        )
    }
}
