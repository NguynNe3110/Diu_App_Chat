package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository

class UpdateNotificationPrivacyUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(notificationPrivacy: NotificationPrivacy) {
        settingsRepository.updateNotificationPrivacy(notificationPrivacy)
    }
}
