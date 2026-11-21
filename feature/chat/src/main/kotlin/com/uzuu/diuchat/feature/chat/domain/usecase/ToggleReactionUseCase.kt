package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class ToggleReactionUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        messageId: String,
        mood: MoodType,
    ) = chatRepository.toggleReaction(
        messageId = messageId,
        mood = mood,
    )
}
