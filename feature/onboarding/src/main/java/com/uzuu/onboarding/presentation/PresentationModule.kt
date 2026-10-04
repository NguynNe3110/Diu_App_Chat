package com.uzuu.onboarding.presentation

import com.uzuu.onboarding.presentation.screen.OnBoardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::OnBoardingViewModel)
}
