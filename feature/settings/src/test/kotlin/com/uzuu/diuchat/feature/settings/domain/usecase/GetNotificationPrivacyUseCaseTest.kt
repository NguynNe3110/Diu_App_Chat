package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetNotificationPrivacyUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()

    val sut = GetNotificationPrivacyUseCase(settingsRepository)

    @Test
    fun `getNotificationPrivacy delegates to repository`() {
        runBlocking {
            val expected = NotificationPrivacy.MOOD_COLOR_ONLY
            every { settingsRepository.getNotificationPrivacy() } returns flowOf(expected)

            val actual = sut().first()

            actual shouldBeEqualTo expected
        }
    }
}
