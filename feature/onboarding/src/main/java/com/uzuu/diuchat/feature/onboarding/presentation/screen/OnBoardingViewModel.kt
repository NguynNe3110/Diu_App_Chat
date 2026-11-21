package com.uzuu.diuchat.feature.onboarding.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.onboarding.domain.usecase.CompleteOnboardingUseCase
import kotlinx.coroutines.launch

class OnBoardingViewModel(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) : BaseViewModel<OnBoardingUiState, OnBoardingAction>(
    initialState = OnBoardingUiState.Initial,
) {
    fun onPageChanged(page: Int) {
        sendAction(OnBoardingAction.NavigateToPage(page))
    }

    fun onOpenPermissionsStep() {
        sendAction(OnBoardingAction.OpenPermissionsStep)
    }

    fun onBackFromPermissions() {
        sendAction(OnBoardingAction.BackToSlides)
    }

    fun onNotificationsToggled(enabled: Boolean) {
        sendAction(OnBoardingAction.SetNotificationsEnabled(enabled))
    }

    fun onMicrophoneToggled(enabled: Boolean) {
        sendAction(OnBoardingAction.SetMicrophoneEnabled(enabled))
    }

    fun onQuietHoursToggled(enabled: Boolean) {
        sendAction(OnBoardingAction.SetQuietHoursEnabled(enabled))
    }

    fun onCompleteOnboarding(onFinish: () -> Unit) {
        val state = uiStateFlow.value
        if (state.isCompleting) return

        sendAction(OnBoardingAction.SetCompleting(true))
        viewModelScope.launch {
            runCatching {
                completeOnboardingUseCase(
                    notificationsEnabled = state.notificationsEnabled,
                    microphoneEnabled = state.microphoneEnabled,
                    quietHoursEnabled = state.quietHoursEnabled,
                )
            }.onSuccess {
                sendAction(OnBoardingAction.SetCompleting(false))
                onFinish()
            }.onFailure {
                sendAction(OnBoardingAction.SetCompleting(false))
                onFinish()
            }
        }
    }
}
