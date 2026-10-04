package com.uzuu.onboarding.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel

class OnBoardingViewModel : BaseViewModel<OnBoardingUiState, OnBoardingAction>(
    initialState = OnBoardingUiState.Initial,
) {
    fun onPageChanged(page: Int) {
        sendAction(OnBoardingAction.NavigateToPage(page))
    }
}
