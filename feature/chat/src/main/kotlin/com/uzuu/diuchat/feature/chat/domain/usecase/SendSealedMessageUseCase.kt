package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class SendSealedMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        content: String,
        mood: MoodType,
        unlockAtEpochMs: Long,
        scheduledLabel: String,
    ): ChatMessage = chatRepository.sendSealedMessage(
        conversationId = conversationId,
        content = content,
        mood = mood,
        unlockAtEpochMs = unlockAtEpochMs,
        scheduledLabel = scheduledLabel,
    )
}
