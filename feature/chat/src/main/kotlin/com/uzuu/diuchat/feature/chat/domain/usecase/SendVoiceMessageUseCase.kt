package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class SendVoiceMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        audioUri: String,
        durationMs: Long,
        waveform: List<Float>,
        mood: MoodType,
        replyToId: String? = null,
        replyQuote: String? = null,
    ): ChatMessage = chatRepository.sendVoiceMessage(
        conversationId = conversationId,
        audioUri = audioUri,
        durationMs = durationMs,
        waveform = waveform,
        mood = mood,
        replyToId = replyToId,
        replyQuote = replyQuote,
    )
}
