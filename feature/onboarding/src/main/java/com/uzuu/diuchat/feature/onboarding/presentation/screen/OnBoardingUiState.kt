package com.uzuu.diuchat.feature.onboarding.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState

data class OnBoardingUiState(
    val currentPage: Int = 0,
    val showPermissionsStep: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val microphoneEnabled: Boolean = false,
    val quietHoursEnabled: Boolean = true,
    val isCompleting: Boolean = false,
) : BaseState {
    companion object {
        val Initial = OnBoardingUiState(currentPage = 0)
    }
}
