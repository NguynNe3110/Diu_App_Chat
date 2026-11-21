package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class TogglePinMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        messageId: String,
    ) = chatRepository.togglePinMessage(
        conversationId = conversationId,
        messageId = messageId,
    )
}
