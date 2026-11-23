package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class LoadOlderMessagesUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        beforeEpochMs: Long,
        limit: Int = ChatRepository.DEFAULT_PAGE_SIZE,
    ): Boolean = chatRepository.loadOlderMessages(
        conversationId = conversationId,
        beforeEpochMs = beforeEpochMs,
        limit = limit,
    )
}
