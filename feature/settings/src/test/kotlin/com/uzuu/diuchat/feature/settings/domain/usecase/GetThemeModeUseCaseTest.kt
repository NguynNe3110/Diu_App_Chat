package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetThemeModeUseCaseTest {
    private val settingsRepository: SettingsRepository = mockk()

    val sut = GetThemeModeUseCase(settingsRepository)

    @Test
    fun `delegates to repository getThemeMode`() = runTest {
        every { settingsRepository.getThemeMode() } returns flowOf("DARK")

        val actual = sut().first()

        actual shouldBeEqualTo "DARK"
    }
}
