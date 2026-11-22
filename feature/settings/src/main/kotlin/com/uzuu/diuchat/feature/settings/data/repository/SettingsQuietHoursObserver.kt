package com.uzuu.diuchat.feature.settings.data.repository

import com.uzuu.diuchat.feature.base.domain.repository.QuietHoursObserver
import com.uzuu.diuchat.feature.base.domain.repository.QuietHoursStatus
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

internal class SettingsQuietHoursObserver(
    private val settingsRepository: SettingsRepository,
) : QuietHoursObserver {
    override fun observeQuietHoursStatus(): Flow<QuietHoursStatus> = settingsRepository.getQuietSchedule().map { schedule ->
        val now = Calendar.getInstance()
        val minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * MINUTES_PER_HOUR + now.get(Calendar.MINUTE)
        val isActive = schedule.enabled && schedule.isInQuietHours(minuteOfDay)
        QuietHoursStatus(
            isActive = isActive,
            bannerText = if (isActive) {
                "Giờ yên lặng đang bật · đến ${schedule.endTimeFormatted}"
            } else {
                ""
            },
        )
    }

    private companion object {
        const val MINUTES_PER_HOUR = 60
    }
}
