package com.uzuu.diuchat.feature.settings.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserSettingsDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("theme_mode")
    val themeMode: String? = DEFAULT_THEME_MODE,
    @SerialName("read_receipts_enabled")
    val readReceiptsEnabled: Boolean? = true,
    @SerialName("soft_send_animation_enabled")
    val softSendAnimationEnabled: Boolean? = true,
    @SerialName("vibrate_once_enabled")
    val vibrateOnceEnabled: Boolean? = true,
    @SerialName("evening_group_digest_enabled")
    val eveningGroupDigestEnabled: Boolean? = true,
    @SerialName("notification_privacy")
    val notificationPrivacy: String = DEFAULT_NOTIFICATION_PRIVACY,
    @SerialName("notification_sound")
    val notificationSound: String = DEFAULT_NOTIFICATION_SOUND,
    @SerialName("quiet_hours_enabled")
    val quietHoursEnabled: Boolean = true,
    @SerialName("quiet_hours_start_minute")
    val quietHoursStartMinute: Int = DEFAULT_QUIET_START_MINUTE,
    @SerialName("quiet_hours_end_minute")
    val quietHoursEndMinute: Int = DEFAULT_QUIET_END_MINUTE,
    @SerialName("quiet_hours_timezone")
    val quietHoursTimezone: String = DEFAULT_TIMEZONE,
    @SerialName("quiet_hours_allowed_contacts")
    val quietHoursAllowedContacts: List<String> = emptyList(),
    @SerialName("quiet_hours_repeat_days")
    val quietHoursRepeatDays: List<Int> = DEFAULT_REPEAT_DAYS,
) {
    companion object {
        const val DEFAULT_THEME_MODE = "SYSTEM"
        const val DEFAULT_NOTIFICATION_PRIVACY = "FULL_CONTENT"
        const val DEFAULT_NOTIFICATION_SOUND = "WATER_DROP"
        const val DEFAULT_QUIET_START_MINUTE = 22 * 60 + 30
        const val DEFAULT_QUIET_END_MINUTE = 6 * 60
        const val DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh"
        val DEFAULT_REPEAT_DAYS = listOf(1, 2, 3, 4, 5, 6, 7)
    }
}
