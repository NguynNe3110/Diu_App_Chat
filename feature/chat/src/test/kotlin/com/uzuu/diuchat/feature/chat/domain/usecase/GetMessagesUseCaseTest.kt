package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetMessagesUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = GetMessagesUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository observeMessages`() = runTest {
        // given
        val conversationId = "conv_1"
        val expected =
            listOf(
                ChatMessage(
                    id = "msg_1",
                    conversationId = conversationId,
                    senderId = "user_1",
                    senderName = "Linh",
                    content = "Chào anh",
                    mood = MoodType.THUONG,
                    status = MessageStatus.SENT,
                ),
            )
        every { chatRepository.observeMessages(conversationId) } returns flowOf(expected)

        // when
        val actual = sut(conversationId).first()

        // then
        actual shouldBeEqualTo expected
        verify(exactly = 1) { chatRepository.observeMessages(conversationId) }
    }
}
