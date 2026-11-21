package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class LogoutUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()

    val sut = LogoutUseCase(settingsRepository)

    @Test
    fun `logout delegates to repository`() {
        runBlocking {
            coEvery { settingsRepository.logout() } returns Unit

            sut()

            coVerify { settingsRepository.logout() }
        }
    }
}
