package com.uzuu.diuchat.feature.auth.domain

import com.uzuu.diuchat.feature.auth.domain.usecase.SendOtpUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.VerifyOtpUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::SendOtpUseCase)
        singleOf(::VerifyOtpUseCase)
    }
