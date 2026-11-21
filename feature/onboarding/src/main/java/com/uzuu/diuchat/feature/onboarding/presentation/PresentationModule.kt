package com.uzuu.diuchat.feature.onboarding.presentation

import com.uzuu.diuchat.feature.onboarding.presentation.screen.OnBoardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::OnBoardingViewModel)
}
