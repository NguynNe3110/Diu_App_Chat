package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class UnblockContactUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    val sut = UnblockContactUseCase(settingsRepository)

    @Test
    fun `unblockContact delegates to repository`() {
        runBlocking {
            coEvery { settingsRepository.unblockContact("block_1") } returns Unit

            sut("block_1")

            coVerify { settingsRepository.unblockContact("block_1") }
        }
    }
}
