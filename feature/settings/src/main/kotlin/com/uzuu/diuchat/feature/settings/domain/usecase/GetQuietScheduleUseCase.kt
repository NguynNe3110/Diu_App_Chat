package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetQuietScheduleUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<QuietSchedule> = settingsRepository.getQuietSchedule()
}
