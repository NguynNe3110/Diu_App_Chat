package com.uzuu.onboarding.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction

sealed interface OnBoardingAction : BaseAction<OnBoardingUiState> {

    /** Người dùng nhấn Next hoặc vuốt trang tiếp theo */
    data class NavigateToPage(val page: Int) : OnBoardingAction {
        override fun reduce(state: OnBoardingUiState) = state.copy(currentPage = page)
    }
}
