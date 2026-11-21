package com.uzuu.diuchat.feature.onboarding.domain.usecase

import com.uzuu.diuchat.feature.onboarding.domain.repository.OnBoardingRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test

class SetOnboardingCompletedUseCaseTest {
    private val onBoardingRepository: OnBoardingRepository = mockk()
    private val sut = SetOnboardingCompletedUseCase(onBoardingRepository)

    @Test
    fun `invoke delegates to repository setOnboardingCompleted`() {
        every { onBoardingRepository.setOnboardingCompleted(any()) } just runs

        sut(true)

        verify(exactly = 1) { onBoardingRepository.setOnboardingCompleted(true) }
    }
}
