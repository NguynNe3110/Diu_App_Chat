package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class SendPhotoMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        photoUri: String,
        caption: String?,
        mood: MoodType,
        replyToId: String? = null,
        replyQuote: String? = null,
    ): ChatMessage = chatRepository.sendPhotoMessage(
        conversationId = conversationId,
        photoUri = photoUri,
        caption = caption,
        mood = mood,
        replyToId = replyToId,
        replyQuote = replyQuote,
    )
}
