package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class UnblockContactUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(contactId: String) {
        settingsRepository.unblockContact(contactId)
    }
}
