package com.uzuu.diuchat.feature.settings.data.mapper

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule

internal data class UserSettingsSnapshot(
    val themeMode: String,
    val readReceiptsEnabled: Boolean,
    val softSendAnimationEnabled: Boolean,
    val notificationPrivacy: NotificationPrivacy,
    val notificationSound: NotificationSound,
    val quietSchedule: QuietSchedule,
)

internal class UserSettingsMapper {
    fun toQuietSchedule(dto: UserSettingsDto): QuietSchedule = QuietSchedule(
        enabled = dto.quietHoursEnabled,
        startMinuteOfDay = dto.quietHoursStartMinute,
        endMinuteOfDay = dto.quietHoursEndMinute,
        timeZoneId = dto.quietHoursTimezone,
        allowedEmergencyContactIds = dto.quietHoursAllowedContacts.toSet(),
        repeatDays = dto.quietHoursRepeatDays.toSet().ifEmpty { QuietSchedule.DEFAULT_REPEAT_DAYS },
    )

    fun toNotificationPrivacy(dto: UserSettingsDto): NotificationPrivacy = runCatching {
        NotificationPrivacy.valueOf(dto.notificationPrivacy)
    }.getOrDefault(NotificationPrivacy.FULL_CONTENT)

    fun toNotificationSound(dto: UserSettingsDto): NotificationSound = runCatching {
        NotificationSound.valueOf(dto.notificationSound)
    }.getOrDefault(NotificationSound.WATER_DROP)

    fun toDto(
        userId: String,
        snapshot: UserSettingsSnapshot,
    ): UserSettingsDto = UserSettingsDto(
        userId = userId,
        themeMode = snapshot.themeMode,
        readReceiptsEnabled = snapshot.readReceiptsEnabled,
        softSendAnimationEnabled = snapshot.softSendAnimationEnabled,
        notificationPrivacy = snapshot.notificationPrivacy.name,
        notificationSound = snapshot.notificationSound.name,
        quietHoursEnabled = snapshot.quietSchedule.enabled,
        quietHoursStartMinute = snapshot.quietSchedule.startMinuteOfDay,
        quietHoursEndMinute = snapshot.quietSchedule.endMinuteOfDay,
        quietHoursTimezone = snapshot.quietSchedule.timeZoneId,
        quietHoursAllowedContacts = snapshot.quietSchedule.allowedEmergencyContactIds.toList(),
        quietHoursRepeatDays = snapshot.quietSchedule.repeatDays.sorted(),
    )
}
