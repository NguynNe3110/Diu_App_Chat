package com.uzuu.diuchat.feature.onboarding.domain.repository

import com.uzuu.diuchat.feature.onboarding.domain.model.OnboardingPreferences
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val onboardingCompletedFlow: Flow<Boolean>

    val onboardingPreferencesFlow: Flow<OnboardingPreferences>

    suspend fun completeOnboarding(
        notificationsEnabled: Boolean,
        microphoneEnabled: Boolean,
        quietHoursEnabled: Boolean,
    )
}
