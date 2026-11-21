package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateNotificationSoundUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val sut = UpdateNotificationSoundUseCase(settingsRepository)

    @Test
    fun `invoke delegates to repository updateNotificationSound`() = runTest {
        coEvery { settingsRepository.updateNotificationSound(NotificationSound.FALLING_LEAF) } returns Unit

        sut(NotificationSound.FALLING_LEAF)

        coVerify(exactly = 1) { settingsRepository.updateNotificationSound(NotificationSound.FALLING_LEAF) }
    }
}
