package com.uzuu.diuchat.feature.settings.presentation

import com.uzuu.diuchat.feature.settings.presentation.screen.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        SettingsViewModel(
            getBlockedContactsUseCase = get(),
            getNotificationPrivacyUseCase = get(),
            getQuietScheduleUseCase = get(),
            unblockContactUseCase = get(),
            updateNotificationPrivacyUseCase = get(),
            updateQuietScheduleUseCase = get(),
        )
    }
}
