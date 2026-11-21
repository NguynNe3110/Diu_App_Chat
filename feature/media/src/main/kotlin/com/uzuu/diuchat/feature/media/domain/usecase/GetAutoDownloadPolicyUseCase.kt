package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetAutoDownloadPolicyUseCase(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(): Flow<AutoDownloadPolicy> = mediaRepository.getAutoDownloadPolicy()
}
