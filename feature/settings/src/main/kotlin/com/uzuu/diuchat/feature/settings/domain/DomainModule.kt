package com.uzuu.diuchat.feature.settings.domain

import com.uzuu.diuchat.feature.settings.domain.usecase.DeleteAccountUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetBlockedContactsUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.LogoutUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UnblockContactUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateQuietScheduleUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { DeleteAccountUseCase(get()) }
    factory { GetBlockedContactsUseCase(get()) }
    factory { GetNotificationPrivacyUseCase(get()) }
    factory { GetProfileUseCase(get()) }
    factory { GetQuietScheduleUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { UnblockContactUseCase(get()) }
    factory { UpdateNotificationPrivacyUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { UpdateQuietScheduleUseCase(get()) }
}
