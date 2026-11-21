package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetVaultMediaUseCase(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(conversationId: String): Flow<List<MediaVaultItem>> = mediaRepository.getVaultMedia(conversationId)
}
