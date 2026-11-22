package com.uzuu.diuchat.feature.settings.presentation

import com.uzuu.diuchat.feature.settings.presentation.screen.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        SettingsViewModel(
            deleteAccountUseCase = get(),
            getBlockedContactsUseCase = get(),
            getEveningGroupDigestEnabledUseCase = get(),
            getNotificationPrivacyUseCase = get(),
            getNotificationSoundUseCase = get(),
            getProfileUseCase = get(),
            getQuietScheduleUseCase = get(),
            getReadReceiptsEnabledUseCase = get(),
            getSoftSendAnimationEnabledUseCase = get(),
            getThemeModeUseCase = get(),
            getVibrateOnceEnabledUseCase = get(),
            logoutUseCase = get(),
            unblockContactUseCase = get(),
            updateEveningGroupDigestEnabledUseCase = get(),
            updateNotificationPrivacyUseCase = get(),
            updateNotificationSoundUseCase = get(),
            updateProfileUseCase = get(),
            updateQuietScheduleUseCase = get(),
            updateReadReceiptsEnabledUseCase = get(),
            updateSoftSendAnimationEnabledUseCase = get(),
            updateThemeModeUseCase = get(),
            updateVibrateOnceEnabledUseCase = get(),
        )
    }
}
