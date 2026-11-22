package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveTypingStatusUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(conversationId: String): Flow<Boolean> = chatRepository.observeTypingStatus(conversationId)
}
