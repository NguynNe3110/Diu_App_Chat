package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetVibrateOnceEnabledUseCaseTest {
    private val settingsRepository: SettingsRepository = mockk()
    private val sut = GetVibrateOnceEnabledUseCase(settingsRepository)

    @Test
    fun `invoke returns vibrate once flow from repository`() = runTest {
        every { settingsRepository.getVibrateOnceEnabled() } returns flowOf(false)

        sut().first() shouldBeEqualTo false
    }
}
