package com.uzuu.onboarding.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState

data class OnBoardingUiState(
    val currentPage: Int = 0,
) : BaseState {
    companion object {
        val Initial = OnBoardingUiState(currentPage = 0)
    }
}
