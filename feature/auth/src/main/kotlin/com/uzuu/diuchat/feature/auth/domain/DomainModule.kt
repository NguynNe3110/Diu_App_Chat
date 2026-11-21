package com.uzuu.diuchat.feature.auth.domain

import com.uzuu.diuchat.feature.auth.domain.usecase.CheckHasUsernameUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.ClaimUsernameUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.SendOtpUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.uzuu.diuchat.feature.auth.domain.usecase.VerifyOtpUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::SendOtpUseCase)
        singleOf(::VerifyOtpUseCase)
        singleOf(::SignInWithGoogleUseCase)
        singleOf(::CheckHasUsernameUseCase)
        singleOf(::ClaimUsernameUseCase)
    }
