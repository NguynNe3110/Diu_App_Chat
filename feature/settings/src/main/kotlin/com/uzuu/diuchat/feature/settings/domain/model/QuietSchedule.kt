package com.uzuu.diuchat.feature.settings.domain.model

data class QuietSchedule(
    val enabled: Boolean = true,
    val startMinuteOfDay: Int = 22 * 60 + 30, // 22:30 (1350)
    val endMinuteOfDay: Int = 6 * 60, // 06:00 (360)
    val timeZoneId: String = "Asia/Ho_Chi_Minh",
    val allowedEmergencyContactIds: Set<String> = emptySet(),
) {
    val startTimeFormatted: String
        get() = "%02d:%02d".format(startMinuteOfDay / 60, startMinuteOfDay % 60)

    val endTimeFormatted: String
        get() = "%02d:%02d".format(endMinuteOfDay / 60, endMinuteOfDay % 60)

    val timeRangeFormatted: String
        get() = "$startTimeFormatted – $endTimeFormatted ($timeZoneId)"

    fun isInQuietHours(minuteOfDay: Int): Boolean {
        if (!enabled) return false
        return if (startMinuteOfDay <= endMinuteOfDay) {
            minuteOfDay in startMinuteOfDay..endMinuteOfDay
        } else {
            // Overnight window, e.g. 22:30 to 06:00
            minuteOfDay >= startMinuteOfDay || minuteOfDay < endMinuteOfDay
        }
    }
}
