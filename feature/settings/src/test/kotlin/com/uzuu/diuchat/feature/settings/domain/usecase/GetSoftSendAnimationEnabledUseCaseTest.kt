package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetSoftSendAnimationEnabledUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val sut = GetSoftSendAnimationEnabledUseCase(settingsRepository)

    @Test
    fun `invoke delegates to repository getSoftSendAnimationEnabled`() = runTest {
        every { settingsRepository.getSoftSendAnimationEnabled() } returns flowOf(true)

        val actual = sut().first()

        actual shouldBeEqualTo true
        verify(exactly = 1) { settingsRepository.getSoftSendAnimationEnabled() }
    }
}
