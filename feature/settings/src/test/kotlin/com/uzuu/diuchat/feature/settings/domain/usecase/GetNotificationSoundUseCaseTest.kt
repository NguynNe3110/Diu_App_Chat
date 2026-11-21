package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetNotificationSoundUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val sut = GetNotificationSoundUseCase(settingsRepository)

    @Test
    fun `invoke delegates to repository getNotificationSound`() = runTest {
        every { settingsRepository.getNotificationSound() } returns flowOf(NotificationSound.WIND_CHIME)

        val actual = sut().first()

        actual shouldBeEqualTo NotificationSound.WIND_CHIME
        verify(exactly = 1) { settingsRepository.getNotificationSound() }
    }
}
