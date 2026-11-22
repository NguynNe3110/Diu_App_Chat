package com.uzuu.diuchat.feature.settings.domain

import com.uzuu.diuchat.feature.settings.domain.usecase.DeleteAccountUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetBlockedContactsUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationSoundUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetOnboardingCompletedUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetReadReceiptsEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetSoftSendAnimationEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetThemeModeUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.LogoutUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.SetOnboardingCompletedUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UnblockContactUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationSoundUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateReadReceiptsEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateSoftSendAnimationEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateThemeModeUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { DeleteAccountUseCase(get()) }
    factory { GetBlockedContactsUseCase(get()) }
    factory { GetNotificationPrivacyUseCase(get()) }
    factory { GetNotificationSoundUseCase(get()) }
    factory { GetOnboardingCompletedUseCase(get()) }
    factory { GetProfileUseCase(get()) }
    factory { GetQuietScheduleUseCase(get()) }
    factory { GetReadReceiptsEnabledUseCase(get()) }
    factory { GetSoftSendAnimationEnabledUseCase(get()) }
    factory { GetThemeModeUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { SetOnboardingCompletedUseCase(get()) }
    factory { UnblockContactUseCase(get()) }
    factory { UpdateNotificationPrivacyUseCase(get()) }
    factory { UpdateNotificationSoundUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { UpdateQuietScheduleUseCase(get()) }
    factory { UpdateReadReceiptsEnabledUseCase(get()) }
    factory { UpdateSoftSendAnimationEnabledUseCase(get()) }
    factory { UpdateThemeModeUseCase(get()) }
}
