package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TogglePinMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = TogglePinMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository togglePinMessage`() = runTest {
        // given
        val convoId = "conv_1"
        val messageId = "msg_456"
        coEvery { chatRepository.togglePinMessage(convoId, messageId) } just runs

        // when
        sut(convoId, messageId)

        // then
        coVerify(exactly = 1) { chatRepository.togglePinMessage(convoId, messageId) }
    }
}
