package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository

class CreateGroupUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        title: String,
        mood: MoodType,
        memberIds: List<String>,
    ): Conversation = chatRepository.createGroup(
        title = title,
        mood = mood,
        memberIds = memberIds,
    )
}
