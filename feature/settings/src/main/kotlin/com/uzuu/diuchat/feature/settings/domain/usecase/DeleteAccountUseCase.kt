package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class DeleteAccountUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(confirmPhone: String) {
        settingsRepository.deleteAccount(confirmPhone)
    }
}
