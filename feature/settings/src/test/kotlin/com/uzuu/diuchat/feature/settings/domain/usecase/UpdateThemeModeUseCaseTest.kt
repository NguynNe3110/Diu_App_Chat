package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateThemeModeUseCaseTest {
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    val sut = UpdateThemeModeUseCase(settingsRepository)

    @Test
    fun `delegates to repository updateThemeMode`() = runTest {
        coEvery { settingsRepository.updateThemeMode("LIGHT") } returns Unit

        sut("LIGHT")

        coVerify(exactly = 1) {
            settingsRepository.updateThemeMode("LIGHT")
        }
    }
}
