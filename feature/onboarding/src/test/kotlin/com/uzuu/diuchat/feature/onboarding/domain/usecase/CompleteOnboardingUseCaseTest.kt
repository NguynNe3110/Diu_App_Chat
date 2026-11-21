package com.uzuu.diuchat.feature.onboarding.domain.usecase

import com.uzuu.diuchat.feature.onboarding.domain.repository.OnboardingRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CompleteOnboardingUseCaseTest {
    private val repository: OnboardingRepository = mockk(relaxed = true)
    private val sut = CompleteOnboardingUseCase(onboardingRepository = repository)

    @Test
    fun `invoke delegates completion preferences to repository`() = runTest {
        sut(
            notificationsEnabled = true,
            microphoneEnabled = false,
            quietHoursEnabled = true,
        )

        coVerify(exactly = 1) {
            repository.completeOnboarding(
                notificationsEnabled = true,
                microphoneEnabled = false,
                quietHoursEnabled = true,
            )
        }
    }
}
