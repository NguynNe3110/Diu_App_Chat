package com.uzuu.diuchat.feature.onboarding.data.repository

import com.uzuu.diuchat.feature.onboarding.data.datasource.preferences.OnboardingPreferencesDataSource
import com.uzuu.diuchat.feature.onboarding.domain.model.OnboardingPreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class OnboardingRepositoryImplTest {
    private val dataSource: OnboardingPreferencesDataSource = mockk(relaxed = true) {
        every { onboardingCompletedFlow } returns flowOf(false)
        every { onboardingPreferencesFlow } returns flowOf(OnboardingPreferences())
    }
    private val sut = OnboardingRepositoryImpl(preferencesDataSource = dataSource)

    @Test
    fun `onboardingCompletedFlow mirrors data source`() = runTest {
        sut.onboardingCompletedFlow.first() shouldBeEqualTo false
    }

    @Test
    fun `completeOnboarding saves flags to preferences data source`() = runTest {
        sut.completeOnboarding(
            notificationsEnabled = true,
            microphoneEnabled = true,
            quietHoursEnabled = false,
        )

        coVerify(exactly = 1) {
            dataSource.saveOnboardingCompletion(
                notificationsEnabled = true,
                microphoneEnabled = true,
                quietHoursEnabled = false,
            )
        }
    }
}
