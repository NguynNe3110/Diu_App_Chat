package com.uzuu.diuchat.feature.onboarding.domain.usecase

import com.uzuu.diuchat.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class GetOnboardingCompletedUseCase(
    private val onboardingRepository: OnboardingRepository,
) {
    operator fun invoke(): Flow<Boolean> = onboardingRepository.onboardingCompletedFlow
}
