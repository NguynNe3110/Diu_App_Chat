package com.uzuu.diuchat.feature.onboarding.domain.usecase

import com.uzuu.diuchat.feature.onboarding.domain.repository.OnboardingRepository

class CompleteOnboardingUseCase(
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(
        notificationsEnabled: Boolean,
        microphoneEnabled: Boolean,
        quietHoursEnabled: Boolean,
    ) {
        onboardingRepository.completeOnboarding(
            notificationsEnabled = notificationsEnabled,
            microphoneEnabled = microphoneEnabled,
            quietHoursEnabled = quietHoursEnabled,
        )
    }
}
