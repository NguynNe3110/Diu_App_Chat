package com.uzuu.diuchat.feature.onboarding.domain

import com.uzuu.diuchat.feature.onboarding.domain.usecase.IsOnboardingCompletedUseCase
import com.uzuu.diuchat.feature.onboarding.domain.usecase.SetOnboardingCompletedUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule =
    module {
        factoryOf(::IsOnboardingCompletedUseCase)
        factoryOf(::SetOnboardingCompletedUseCase)
    }
