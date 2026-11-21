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

class SendVoiceMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = SendVoiceMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository sendVoiceMessage`() = runTest {
        // given
        val conversationId = "conv_1"
        val audioUri = "file:///cache/audio_1.m4a"
        val durationMs = 8500L
        val waveform = listOf(0.2f, 0.6f, 0.8f, 0.5f)
        val mood = MoodType.THUONG
        val expected =
            ChatMessage(
                id = "msg_voice_1",
                conversationId = conversationId,
                senderId = "me",
                senderName = "Tôi",
                content = "🎤 [Tin nhắn thoại]",
                mood = mood,
                status = MessageStatus.SENT,
                mediaType = MessageMediaType.VOICE,
                mediaUri = audioUri,
                audioDurationMs = durationMs,
                audioWaveform = waveform,
            )

        coEvery {
            chatRepository.sendVoiceMessage(
                conversationId = conversationId,
                audioUri = audioUri,
                durationMs = durationMs,
                waveform = waveform,
                mood = mood,
                replyToId = null,
                replyQuote = null,
            )
        } returns expected

        // when
        val actual =
            sut(
                conversationId = conversationId,
                audioUri = audioUri,
                durationMs = durationMs,
                waveform = waveform,
                mood = mood,
            )

        // then
        actual shouldBeEqualTo expected
        coVerify(exactly = 1) {
            chatRepository.sendVoiceMessage(
                conversationId = conversationId,
                audioUri = audioUri,
                durationMs = durationMs,
                waveform = waveform,
                mood = mood,
                replyToId = null,
                replyQuote = null,
            )
        }
    }
}
