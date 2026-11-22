package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SendSealedMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = SendSealedMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates to chatRepository sendSealedMessage`() = runTest {
        val expected = ChatMessage(
            id = "sealed_1",
            conversationId = "conv_1",
            senderId = "me",
            senderName = "Tôi",
            content = "Chúc ngủ ngon",
            mood = MoodType.THUONG,
            status = MessageStatus.SENT,
            mediaType = MessageMediaType.SEALED_LETTER,
            scheduledForLabel = "22:00 Tối nay",
        )
        coEvery {
            chatRepository.sendSealedMessage("conv_1", "Chúc ngủ ngon", MoodType.THUONG, 999_000L, "22:00 Tối nay")
        } returns expected

        val result = sut("conv_1", "Chúc ngủ ngon", MoodType.THUONG, 999_000L, "22:00 Tối nay")

        result shouldBeEqualTo expected
        coVerify(exactly = 1) {
            chatRepository.sendSealedMessage("conv_1", "Chúc ngủ ngon", MoodType.THUONG, 999_000L, "22:00 Tối nay")
        }
    }
}
