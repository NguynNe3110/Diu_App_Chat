package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SendTypingStatusUseCaseTest {
    private val repository: ChatRepository = mockk()
    private val sut = SendTypingStatusUseCase(repository)

    @Test
    fun `invoke delegates to repository sendTypingStatus`() = runTest {
        coEvery { repository.sendTypingStatus("conv_1", true) } returns Unit

        sut("conv_1", true)

        coVerify(exactly = 1) { repository.sendTypingStatus("conv_1", true) }
    }
}
