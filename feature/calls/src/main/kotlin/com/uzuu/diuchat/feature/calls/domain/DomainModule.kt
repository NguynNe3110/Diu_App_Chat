package com.uzuu.diuchat.feature.calls.domain

import com.uzuu.diuchat.feature.calls.domain.usecase.AcceptCallUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.EndCallUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.GetCallHistoryUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.StartCallUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { AcceptCallUseCase(get()) }
    factory { EndCallUseCase(get()) }
    factory { GetCallHistoryUseCase(get()) }
    factory { StartCallUseCase(get()) }
}
