package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetThemeModeUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<String> = settingsRepository.getThemeMode()
}
