package com.uzuu.diuchat.feature.onboarding.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction

sealed interface OnBoardingAction : BaseAction<OnBoardingUiState> {
    /** Người dùng nhấn Next hoặc vuốt trang tiếp theo. */
    data class NavigateToPage(
        val page: Int,
    ) : OnBoardingAction {
        override fun reduce(state: OnBoardingUiState) = state.copy(currentPage = page)
    }

    /** Chuyển sang bước #06 Quyền truy cập. */
    data object OpenPermissionsStep : OnBoardingAction {
        override fun reduce(state: OnBoardingUiState) = state.copy(showPermissionsStep = true)
    }

    /** Quay lại các slide giới thiệu. */
    data object BackToSlides : OnBoardingAction {
        override fun reduce(state: OnBoardingUiState) = state.copy(showPermissionsStep = false)
    }

    data class SetNotificationsEnabled(
        val enabled: Boolean,
    ) : OnBoardingAction {
        override fun reduce(state: OnBoardingUiState) = state.copy(notificationsEnabled = enabled)
    }

    data class SetMicrophoneEnabled(
        val enabled: Boolean,
    ) : OnBoardingAction {
        override fun reduce(state: OnBoardingUiState) = state.copy(microphoneEnabled = enabled)
    }

    data class SetQuietHoursEnabled(
        val enabled: Boolean,
    ) : OnBoardingAction {
        override fun reduce(state: OnBoardingUiState) = state.copy(quietHoursEnabled = enabled)
    }

    data class SetCompleting(
        val isCompleting: Boolean,
    ) : OnBoardingAction {
        override fun reduce(state: OnBoardingUiState) = state.copy(isCompleting = isCompleting)
    }
}
