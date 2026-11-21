package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class ForwardMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        message: ChatMessage,
        recipientConversationIds: List<String>,
    ): List<ChatMessage> = chatRepository.forwardMessage(
        message = message,
        recipientConversationIds = recipientConversationIds,
    )
}
