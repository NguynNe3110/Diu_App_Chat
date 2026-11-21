package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateReadReceiptsEnabledUseCaseTest {
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    val sut = UpdateReadReceiptsEnabledUseCase(settingsRepository)

    @Test
    fun `delegates to repository updateReadReceiptsEnabled`() = runTest {
        coEvery { settingsRepository.updateReadReceiptsEnabled(false) } returns Unit

        sut(false)

        coVerify(exactly = 1) {
            settingsRepository.updateReadReceiptsEnabled(false)
        }
    }
}
