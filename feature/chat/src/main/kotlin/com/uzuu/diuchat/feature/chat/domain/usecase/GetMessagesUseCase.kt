package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetMessagesUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(conversationId: String): Flow<List<ChatMessage>> = chatRepository.observeMessages(conversationId)
}
