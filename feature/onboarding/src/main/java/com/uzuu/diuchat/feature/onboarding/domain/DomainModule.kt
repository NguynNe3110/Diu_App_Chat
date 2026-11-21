package com.uzuu.diuchat.feature.onboarding.domain

import com.uzuu.diuchat.feature.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.uzuu.diuchat.feature.onboarding.domain.usecase.GetOnboardingCompletedUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule =
    module {
        factoryOf(::CompleteOnboardingUseCase)
        factoryOf(::GetOnboardingCompletedUseCase)
    }
