package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class ManageGroupMemberUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        targetUserId: String,
        newRole: GroupRole? = null,
        isRemove: Boolean = false,
    ) = chatRepository.manageGroupMember(
        conversationId = conversationId,
        targetUserId = targetUserId,
        newRole = newRole,
        isRemove = isRemove,
    )
}
