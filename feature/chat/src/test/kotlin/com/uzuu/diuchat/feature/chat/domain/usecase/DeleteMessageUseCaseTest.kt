package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = DeleteMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates to chatRepository deleteMessage`() = runTest {
        coEvery { chatRepository.deleteMessage("msg_1") } just runs

        sut("msg_1")

        coVerify(exactly = 1) { chatRepository.deleteMessage("msg_1") }
    }
}
