package com.uzuu.diuchat.feature.contacts.domain

import com.uzuu.diuchat.feature.contacts.domain.usecase.GetContactsUseCase
import com.uzuu.diuchat.feature.contacts.domain.usecase.GetPendingRequestsUseCase
import com.uzuu.diuchat.feature.contacts.domain.usecase.RespondConnectionRequestUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetContactsUseCase)
        singleOf(::GetPendingRequestsUseCase)
        singleOf(::RespondConnectionRequestUseCase)
    }
