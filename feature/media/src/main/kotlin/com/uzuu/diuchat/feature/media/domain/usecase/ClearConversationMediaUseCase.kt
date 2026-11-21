package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository

class ClearConversationMediaUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(conversationId: String): Result<Long> = mediaRepository.clearConversationMedia(conversationId)
}
