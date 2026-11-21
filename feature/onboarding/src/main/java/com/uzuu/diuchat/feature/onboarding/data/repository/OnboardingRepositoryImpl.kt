package com.uzuu.diuchat.feature.onboarding.data.repository

import com.uzuu.diuchat.feature.onboarding.data.datasource.preferences.OnboardingPreferencesDataSource
import com.uzuu.diuchat.feature.onboarding.domain.model.OnboardingPreferences
import com.uzuu.diuchat.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class OnboardingRepositoryImpl(
    private val preferencesDataSource: OnboardingPreferencesDataSource,
) : OnboardingRepository {
    override val onboardingCompletedFlow: Flow<Boolean> = preferencesDataSource.onboardingCompletedFlow

    override val onboardingPreferencesFlow: Flow<OnboardingPreferences> = preferencesDataSource.onboardingPreferencesFlow

    override suspend fun completeOnboarding(
        notificationsEnabled: Boolean,
        microphoneEnabled: Boolean,
        quietHoursEnabled: Boolean,
    ) {
        preferencesDataSource.saveOnboardingCompletion(
            notificationsEnabled = notificationsEnabled,
            microphoneEnabled = microphoneEnabled,
            quietHoursEnabled = quietHoursEnabled,
        )
    }
}
