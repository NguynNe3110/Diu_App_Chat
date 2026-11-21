package com.uzuu.diuchat.feature.settings.data.mapper

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class UserSettingsMapperTest {
    private val mapper = UserSettingsMapper()

    @Test
    fun `toQuietSchedule maps fields and falls back to default repeatDays when empty`() {
        val dto = UserSettingsDto(
            userId = "u-1",
            quietHoursEnabled = false,
            quietHoursStartMinute = 1320,
            quietHoursEndMinute = 420,
            quietHoursTimezone = "Asia/Tokyo",
            quietHoursAllowedContacts = listOf("c1", "c2"),
            quietHoursRepeatDays = emptyList(),
        )

        val schedule = mapper.toQuietSchedule(dto)

        schedule shouldBeEqualTo QuietSchedule(
            enabled = false,
            startMinuteOfDay = 1320,
            endMinuteOfDay = 420,
            timeZoneId = "Asia/Tokyo",
            allowedEmergencyContactIds = setOf("c1", "c2"),
            repeatDays = QuietSchedule.DEFAULT_REPEAT_DAYS,
        )
    }

    @Test
    fun `toNotificationPrivacy and toNotificationSound fall back safely on unknown enum values`() {
        val dto = UserSettingsDto(
            userId = "u-1",
            notificationPrivacy = "UNKNOWN_PRIVACY",
            notificationSound = "UNKNOWN_SOUND",
        )

        mapper.toNotificationPrivacy(dto) shouldBeEqualTo NotificationPrivacy.FULL_CONTENT
        mapper.toNotificationSound(dto) shouldBeEqualTo NotificationSound.WATER_DROP
    }

    @Test
    fun `toDto serializes domain settings cleanly`() {
        val schedule = QuietSchedule(
            enabled = true,
            startMinuteOfDay = 1350,
            endMinuteOfDay = 360,
            timeZoneId = "Asia/Ho_Chi_Minh",
            allowedEmergencyContactIds = setOf("c1"),
            repeatDays = setOf(5, 1, 3),
        )

        val dto = mapper.toDto(
            userId = "u-1",
            snapshot = UserSettingsSnapshot(
                themeMode = "DARK",
                readReceiptsEnabled = false,
                softSendAnimationEnabled = false,
                notificationPrivacy = NotificationPrivacy.MOOD_COLOR_ONLY,
                notificationSound = NotificationSound.WIND_CHIME,
                quietSchedule = schedule,
            ),
        )

        dto shouldBeEqualTo UserSettingsDto(
            userId = "u-1",
            themeMode = "DARK",
            readReceiptsEnabled = false,
            softSendAnimationEnabled = false,
            notificationPrivacy = "MOOD_COLOR_ONLY",
            notificationSound = "WIND_CHIME",
            quietHoursEnabled = true,
            quietHoursStartMinute = 1350,
            quietHoursEndMinute = 360,
            quietHoursTimezone = "Asia/Ho_Chi_Minh",
            quietHoursAllowedContacts = listOf("c1"),
            quietHoursRepeatDays = listOf(1, 3, 5),
        )
    }
}
