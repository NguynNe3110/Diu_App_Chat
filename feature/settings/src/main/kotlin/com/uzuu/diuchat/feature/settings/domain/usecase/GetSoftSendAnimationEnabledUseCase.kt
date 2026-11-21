package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSoftSendAnimationEnabledUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = settingsRepository.getSoftSendAnimationEnabled()
}
