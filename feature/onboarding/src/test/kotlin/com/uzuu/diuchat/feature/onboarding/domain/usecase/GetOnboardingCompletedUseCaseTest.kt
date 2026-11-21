package com.uzuu.diuchat.feature.onboarding.domain.usecase

import com.uzuu.diuchat.feature.onboarding.domain.repository.OnboardingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetOnboardingCompletedUseCaseTest {
    private val repository: OnboardingRepository = mockk {
        every { onboardingCompletedFlow } returns flowOf(true)
    }
    private val sut = GetOnboardingCompletedUseCase(onboardingRepository = repository)

    @Test
    fun `invoke emits onboarding completion status from repository`() = runTest {
        sut().first() shouldBeEqualTo true
    }
}
