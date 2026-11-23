package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class RevealSealedMessageUseCaseTest {
    private val mockChatRepository: ChatRepository = mockk()
    private val sut = RevealSealedMessageUseCase(mockChatRepository)

    @Test
    fun `invoke delegates to chatRepository revealSealedMessage`() = runTest {
        coEvery { mockChatRepository.revealSealedMessage("msg_sealed_1") } returns "Thương em nhiều lắm"

        val content = sut("msg_sealed_1")

        content shouldBeEqualTo "Thương em nhiều lắm"
        coVerify(exactly = 1) { mockChatRepository.revealSealedMessage("msg_sealed_1") }
    }
}
