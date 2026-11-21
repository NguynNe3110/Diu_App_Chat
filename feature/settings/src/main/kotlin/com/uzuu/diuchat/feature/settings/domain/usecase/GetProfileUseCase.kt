package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetProfileUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<UserProfile> = settingsRepository.getUserProfile()
}
