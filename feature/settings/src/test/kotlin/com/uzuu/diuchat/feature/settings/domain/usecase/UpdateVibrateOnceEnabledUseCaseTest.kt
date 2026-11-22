package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateVibrateOnceEnabledUseCaseTest {
    private val settingsRepository: SettingsRepository = mockk()
    private val sut = UpdateVibrateOnceEnabledUseCase(settingsRepository)

    @Test
    fun `invoke delegates to repository updateVibrateOnceEnabled`() = runTest {
        coEvery { settingsRepository.updateVibrateOnceEnabled(false) } just runs

        sut(false)

        coVerify(exactly = 1) { settingsRepository.updateVibrateOnceEnabled(false) }
    }
}
