package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteConversationUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = DeleteConversationUseCase(chatRepository)

    @Test
    fun `invoke delegates to chatRepository deleteConversation`() = runTest {
        coEvery { chatRepository.deleteConversation("conv_1") } just runs

        sut("conv_1")

        coVerify(exactly = 1) { chatRepository.deleteConversation("conv_1") }
    }
}
