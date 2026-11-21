package com.uzuu.diuchat.feature.settings.domain

import com.uzuu.diuchat.feature.settings.domain.usecase.GetBlockedContactsUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UnblockContactUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateQuietScheduleUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetQuietScheduleUseCase(get()) }
    factory { UpdateQuietScheduleUseCase(get()) }
    factory { GetNotificationPrivacyUseCase(get()) }
    factory { UpdateNotificationPrivacyUseCase(get()) }
    factory { GetBlockedContactsUseCase(get()) }
    factory { UnblockContactUseCase(get()) }
}
