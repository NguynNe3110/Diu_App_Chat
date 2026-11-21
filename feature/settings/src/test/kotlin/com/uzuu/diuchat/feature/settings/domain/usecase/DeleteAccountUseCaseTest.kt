package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class DeleteAccountUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()

    val sut = DeleteAccountUseCase(settingsRepository)

    @Test
    fun `deleteAccount delegates to repository with confirmation phone`() {
        runBlocking {
            val confirmPhone = "+84901234567"
            coEvery { settingsRepository.deleteAccount(confirmPhone) } returns Unit

            sut(confirmPhone)

            coVerify { settingsRepository.deleteAccount(confirmPhone) }
        }
    }
}
