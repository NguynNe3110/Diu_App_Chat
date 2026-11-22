package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class SetOnboardingCompletedUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(completed: Boolean) {
        settingsRepository.setOnboardingCompleted(completed)
    }
}
