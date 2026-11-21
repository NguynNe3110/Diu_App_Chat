package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class LeaveGroupUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
    ) = chatRepository.leaveGroup(
        conversationId = conversationId,
    )
}
