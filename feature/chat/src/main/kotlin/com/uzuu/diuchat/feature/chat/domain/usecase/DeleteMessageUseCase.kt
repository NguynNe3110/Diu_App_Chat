package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class DeleteMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(messageId: String) {
        chatRepository.deleteMessage(messageId)
    }
}
