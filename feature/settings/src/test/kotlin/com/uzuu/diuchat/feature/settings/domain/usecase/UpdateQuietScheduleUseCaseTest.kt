package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class UpdateQuietScheduleUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    val sut = UpdateQuietScheduleUseCase(settingsRepository)

    @Test
    fun `updateQuietSchedule delegates to repository`() {
        runBlocking {
            val updated = QuietSchedule(
                enabled = false,
                startMinuteOfDay = 1380,
                endMinuteOfDay = 420,
            )
            coEvery { settingsRepository.updateQuietSchedule(updated) } returns Unit

            sut(updated)

            coVerify { settingsRepository.updateQuietSchedule(updated) }
        }
    }
}
