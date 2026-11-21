package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class UpdateQuietScheduleUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(quietSchedule: QuietSchedule) {
        settingsRepository.updateQuietSchedule(quietSchedule)
    }
}
