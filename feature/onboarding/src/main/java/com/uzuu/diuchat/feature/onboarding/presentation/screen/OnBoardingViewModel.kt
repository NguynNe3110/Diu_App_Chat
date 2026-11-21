package com.uzuu.diuchat.feature.onboarding.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.onboarding.domain.usecase.SetOnboardingCompletedUseCase

class OnBoardingViewModel(
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
) : BaseViewModel<OnBoardingUiState, OnBoardingAction>(
    initialState = OnBoardingUiState.Initial,
) {
    fun onPageChanged(page: Int) {
        sendAction(OnBoardingAction.NavigateToPage(page))
    }

    fun completeOnboarding() {
        sendAction(OnBoardingAction.CompleteOnBoarding)
        setOnboardingCompletedUseCase(true)
    }
}
