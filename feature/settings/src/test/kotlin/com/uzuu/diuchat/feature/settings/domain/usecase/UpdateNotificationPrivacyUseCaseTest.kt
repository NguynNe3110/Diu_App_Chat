package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class UpdateNotificationPrivacyUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    val sut = UpdateNotificationPrivacyUseCase(settingsRepository)

    @Test
    fun `updateNotificationPrivacy delegates to repository`() {
        runBlocking {
            val expected = NotificationPrivacy.SENDER_NAME
            coEvery { settingsRepository.updateNotificationPrivacy(expected) } returns Unit

            sut(expected)

            coVerify { settingsRepository.updateNotificationPrivacy(expected) }
        }
    }
}
