package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateSoftSendAnimationEnabledUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val sut = UpdateSoftSendAnimationEnabledUseCase(settingsRepository)

    @Test
    fun `invoke delegates to repository updateSoftSendAnimationEnabled`() = runTest {
        coEvery { settingsRepository.updateSoftSendAnimationEnabled(false) } returns Unit

        sut(false)

        coVerify(exactly = 1) { settingsRepository.updateSoftSendAnimationEnabled(false) }
    }
}
