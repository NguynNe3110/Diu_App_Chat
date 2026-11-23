package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class RevealSealedMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(messageId: String): String? = chatRepository.revealSealedMessage(messageId)
}
