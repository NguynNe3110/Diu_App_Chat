package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetOnboardingCompletedUseCaseTest {
    private val repository: SettingsRepository = mockk(relaxed = true)
    private val sut = SetOnboardingCompletedUseCase(repository)

    @Test
    fun `invoke delegates to repository setOnboardingCompleted`() = runTest {
        sut(true)

        coVerify(exactly = 1) { repository.setOnboardingCompleted(true) }
    }
}
