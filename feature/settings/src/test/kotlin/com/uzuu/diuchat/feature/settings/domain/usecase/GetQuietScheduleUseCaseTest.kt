package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetQuietScheduleUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()

    val sut = GetQuietScheduleUseCase(settingsRepository)

    @Test
    fun `getQuietSchedule delegates to repository`() {
        runBlocking {
            val expected = QuietSchedule(
                enabled = true,
                startMinuteOfDay = 1350,
                endMinuteOfDay = 360,
                timeZoneId = "Asia/Ho_Chi_Minh",
            )
            every { settingsRepository.getQuietSchedule() } returns flowOf(expected)

            val actual = sut().first()

            actual shouldBeEqualTo expected
        }
    }
}
