package com.uzuu.diuchat.feature.onboarding.domain.usecase

import com.uzuu.diuchat.feature.onboarding.domain.repository.OnBoardingRepository

class SetOnboardingCompletedUseCase(
    private val onBoardingRepository: OnBoardingRepository,
) {
    operator fun invoke(completed: Boolean = true) {
        onBoardingRepository.setOnboardingCompleted(completed)
    }
}
