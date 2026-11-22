package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class StarMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(messageId: String): Boolean = chatRepository.starMessage(messageId)
}
