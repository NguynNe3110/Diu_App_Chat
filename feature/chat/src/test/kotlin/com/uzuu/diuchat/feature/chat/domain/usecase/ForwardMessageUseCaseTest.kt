package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ForwardMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = ForwardMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository forwardMessage`() = runTest {
        // given
        val message = ChatMessage(
            id = "msg_orig",
            conversationId = "conv_1",
            senderId = "me",
            senderName = "Tôi",
            content = "Tin nhắn dịu dàng",
            mood = MoodType.THUONG,
            status = MessageStatus.SENT,
        )
        val targets = listOf("conv_2", "conv_3")
        val expected = listOf(
            message.copy(id = "msg_fwd_1", conversationId = "conv_2"),
            message.copy(id = "msg_fwd_2", conversationId = "conv_3"),
        )

        coEvery { chatRepository.forwardMessage(message, targets) } returns expected

        // when
        val actual = sut(message, targets)

        // then
        actual shouldBeEqualTo expected
        coVerify(exactly = 1) { chatRepository.forwardMessage(message, targets) }
    }
}
