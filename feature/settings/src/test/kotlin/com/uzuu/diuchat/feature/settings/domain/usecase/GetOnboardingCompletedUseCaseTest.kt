package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetOnboardingCompletedUseCaseTest {
    private val repository: SettingsRepository = mockk()
    private val sut = GetOnboardingCompletedUseCase(repository)

    @Test
    fun `invoke returns flow from repository`() = runTest {
        every { repository.getOnboardingCompleted() } returns flowOf(true)

        sut().first() shouldBeEqualTo true
    }
}
