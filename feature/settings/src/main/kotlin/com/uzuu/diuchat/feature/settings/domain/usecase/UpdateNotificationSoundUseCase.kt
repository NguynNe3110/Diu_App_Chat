package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class UpdateNotificationSoundUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(sound: NotificationSound) = settingsRepository.updateNotificationSound(sound)
}
