package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class UpdateProfileUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(userProfile: UserProfile) {
        settingsRepository.updateUserProfile(userProfile)
    }
}
