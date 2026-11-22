package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetConversationsUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(): Flow<List<Conversation>> = chatRepository.observeConversations()
}
