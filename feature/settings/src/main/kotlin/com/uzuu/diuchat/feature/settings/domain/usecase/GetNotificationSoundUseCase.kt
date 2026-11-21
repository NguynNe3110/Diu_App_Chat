package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationSoundUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<NotificationSound> = settingsRepository.getNotificationSound()
}
