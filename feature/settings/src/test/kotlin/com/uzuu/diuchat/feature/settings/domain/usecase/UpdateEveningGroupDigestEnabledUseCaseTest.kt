package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateEveningGroupDigestEnabledUseCaseTest {
    private val settingsRepository: SettingsRepository = mockk()
    private val sut = UpdateEveningGroupDigestEnabledUseCase(settingsRepository)

    @Test
    fun `invoke delegates to repository updateEveningGroupDigestEnabled`() = runTest {
        coEvery { settingsRepository.updateEveningGroupDigestEnabled(false) } just runs

        sut(false)

        coVerify(exactly = 1) { settingsRepository.updateEveningGroupDigestEnabled(false) }
    }
}
