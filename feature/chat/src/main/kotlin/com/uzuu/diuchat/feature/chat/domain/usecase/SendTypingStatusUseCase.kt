package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class SendTypingStatusUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        isTyping: Boolean,
    ) {
        chatRepository.sendTypingStatus(conversationId, isTyping)
    }
}
