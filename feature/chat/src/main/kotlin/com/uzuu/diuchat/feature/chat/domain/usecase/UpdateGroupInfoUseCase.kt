package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class UpdateGroupInfoUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        title: String? = null,
        mood: MoodType? = null,
        nickname: String? = null,
        isMuted: Boolean? = null,
    ) = chatRepository.updateGroupInfo(
        conversationId = conversationId,
        title = title,
        mood = mood,
        nickname = nickname,
        isMuted = isMuted,
    )
}
