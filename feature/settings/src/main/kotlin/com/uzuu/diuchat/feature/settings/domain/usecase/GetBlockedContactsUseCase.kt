package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetBlockedContactsUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<List<BlockedContact>> = settingsRepository.getBlockedContacts()
}
