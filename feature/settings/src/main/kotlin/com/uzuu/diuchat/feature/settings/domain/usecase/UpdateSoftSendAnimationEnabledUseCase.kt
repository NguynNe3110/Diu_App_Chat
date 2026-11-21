package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class UpdateSoftSendAnimationEnabledUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = settingsRepository.updateSoftSendAnimationEnabled(enabled)
}
