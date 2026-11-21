package com.uzuu.diuchat.feature.onboarding.domain.usecase

import com.uzuu.diuchat.feature.onboarding.domain.repository.OnBoardingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class IsOnboardingCompletedUseCaseTest {
    private val onBoardingRepository: OnBoardingRepository = mockk()
    private val sut = IsOnboardingCompletedUseCase(onBoardingRepository)

    @Test
    fun `invoke delegates to repository isOnboardingCompleted`() {
        every { onBoardingRepository.isOnboardingCompleted() } returns true

        val actual = sut()

        actual shouldBeEqualTo true
        verify(exactly = 1) { onBoardingRepository.isOnboardingCompleted() }
    }
}
