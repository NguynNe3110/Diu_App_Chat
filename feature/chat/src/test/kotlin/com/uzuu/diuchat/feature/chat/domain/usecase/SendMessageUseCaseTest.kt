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

class SendMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = SendMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository sendMessage`() = runTest {
        // given
        val conversationId = "conv_1"
        val content = "Em vừa pha tách trà"
        val mood = MoodType.BINHYEN
        val expected =
            ChatMessage(
                id = "msg_123",
                conversationId = conversationId,
                senderId = "me",
                senderName = "Tôi",
                content = content,
                mood = mood,
                status = MessageStatus.SENT,
            )

        coEvery {
            chatRepository.sendMessage(
                conversationId = conversationId,
                content = content,
                mood = mood,
                replyToId = null,
                replyQuote = null,
            )
        } returns expected

        // when
        val actual =
            sut(
                conversationId = conversationId,
                content = content,
                mood = mood,
            )

        // then
        actual shouldBeEqualTo expected
        coVerify(exactly = 1) {
            chatRepository.sendMessage(
                conversationId = conversationId,
                content = content,
                mood = mood,
                replyToId = null,
                replyQuote = null,
            )
        }
    }
}
