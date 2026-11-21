package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class SendMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        content: String,
        mood: MoodType,
        replyToId: String? = null,
        replyQuote: String? = null,
    ): ChatMessage = chatRepository.sendMessage(
        conversationId = conversationId,
        content = content,
        mood = mood,
        replyToId = replyToId,
        replyQuote = replyQuote,
    )
}
