package com.uzuu.diuchat.feature.settings.domain.model

data class QuietSchedule(
    val enabled: Boolean = true,
    val startMinuteOfDay: Int = 22 * 60 + 30, // 22:30 (1350)
    val endMinuteOfDay: Int = 6 * 60, // 06:00 (360)
    val timeZoneId: String = "Asia/Ho_Chi_Minh",
    val allowedEmergencyContactIds: Set<String> = emptySet(),
    val repeatDays: Set<Int> = DEFAULT_REPEAT_DAYS,
) {
    val startTimeFormatted: String
        get() = "%02d:%02d".format(startMinuteOfDay / 60, startMinuteOfDay % 60)

    val endTimeFormatted: String
        get() = "%02d:%02d".format(endMinuteOfDay / 60, endMinuteOfDay % 60)

    val timeRangeFormatted: String
        get() = "$startTimeFormatted – $endTimeFormatted ($timeZoneId)"

    val durationMinutes: Int
        get() = if (endMinuteOfDay >= startMinuteOfDay) {
            endMinuteOfDay - startMinuteOfDay
        } else {
            (24 * 60 - startMinuteOfDay) + endMinuteOfDay
        }

    val durationHoursFormatted: String
        get() {
            val hours = durationMinutes / 60
            val half = (durationMinutes % 60) >= 30
            return if (half) "$hours,5 giờ nghỉ" else "$hours giờ nghỉ"
        }

    fun stepStartMinutes(deltaMinutes: Int): QuietSchedule {
        val next = ((startMinuteOfDay + deltaMinutes) % MINUTES_PER_DAY + MINUTES_PER_DAY) % MINUTES_PER_DAY
        return copy(startMinuteOfDay = next)
    }

    fun stepEndMinutes(deltaMinutes: Int): QuietSchedule {
        val next = ((endMinuteOfDay + deltaMinutes) % MINUTES_PER_DAY + MINUTES_PER_DAY) % MINUTES_PER_DAY
        return copy(endMinuteOfDay = next)
    }

    fun isInQuietHours(minuteOfDay: Int): Boolean {
        if (!enabled) return false
        return if (startMinuteOfDay <= endMinuteOfDay) {
            minuteOfDay in startMinuteOfDay..endMinuteOfDay
        } else {
            // Overnight window, e.g. 22:30 to 06:00
            minuteOfDay >= startMinuteOfDay || minuteOfDay < endMinuteOfDay
        }
    }

    companion object {
        private const val MINUTES_PER_DAY = 1440
        val DEFAULT_REPEAT_DAYS = setOf(1, 2, 3, 4, 5, 6, 7)
    }
}
