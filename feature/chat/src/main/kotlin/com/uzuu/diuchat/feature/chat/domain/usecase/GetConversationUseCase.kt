package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetConversationUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(conversationId: String): Flow<Conversation?> = chatRepository.observeConversation(conversationId)
}
