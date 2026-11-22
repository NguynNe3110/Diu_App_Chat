package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class DeleteConversationUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(conversationId: String) {
        chatRepository.deleteConversation(conversationId)
    }
}
