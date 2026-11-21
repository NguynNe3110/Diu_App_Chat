package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationPrivacyUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<NotificationPrivacy> = settingsRepository.getNotificationPrivacy()
}
