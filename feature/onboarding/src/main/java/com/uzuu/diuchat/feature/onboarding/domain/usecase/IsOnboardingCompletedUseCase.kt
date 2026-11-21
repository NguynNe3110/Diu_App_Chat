package com.uzuu.diuchat.feature.onboarding.domain.usecase

import com.uzuu.diuchat.feature.onboarding.domain.repository.OnBoardingRepository

class IsOnboardingCompletedUseCase(
    private val onBoardingRepository: OnBoardingRepository,
) {
    operator fun invoke(): Boolean = onBoardingRepository.isOnboardingCompleted()
}
