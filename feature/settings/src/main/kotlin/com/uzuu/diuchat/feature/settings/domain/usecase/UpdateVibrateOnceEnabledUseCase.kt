package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class UpdateVibrateOnceEnabledUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.updateVibrateOnceEnabled(enabled)
    }
}
