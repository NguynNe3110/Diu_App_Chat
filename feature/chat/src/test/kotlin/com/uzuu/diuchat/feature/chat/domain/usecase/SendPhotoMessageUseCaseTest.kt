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

class SendPhotoMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = SendPhotoMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository sendPhotoMessage`() = runTest {
        // given
        val conversationId = "conv_1"
        val photoUri = "content://media/external/images/media/42"
        val caption = "Bầu trời chiều nay"
        val mood = MoodType.BINHYEN
        val expected =
            ChatMessage(
                id = "msg_photo_1",
                conversationId = conversationId,
                senderId = "me",
                senderName = "Tôi",
                content = caption,
                mood = mood,
                status = MessageStatus.SENT,
                mediaType = MessageMediaType.PHOTO,
                mediaUri = photoUri,
                caption = caption,
            )

        coEvery {
            chatRepository.sendPhotoMessage(
                conversationId = conversationId,
                photoUri = photoUri,
                caption = caption,
                mood = mood,
                replyToId = null,
                replyQuote = null,
            )
        } returns expected

        // when
        val actual =
            sut(
                conversationId = conversationId,
                photoUri = photoUri,
                caption = caption,
                mood = mood,
            )

        // then
        actual shouldBeEqualTo expected
        coVerify(exactly = 1) {
            chatRepository.sendPhotoMessage(
                conversationId = conversationId,
                photoUri = photoUri,
                caption = caption,
                mood = mood,
                replyToId = null,
                replyQuote = null,
            )
        }
    }
}
