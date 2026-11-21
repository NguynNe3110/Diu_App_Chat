package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository

class UpdateAutoDownloadPolicyUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(policy: AutoDownloadPolicy) {
        mediaRepository.updateAutoDownloadPolicy(policy)
    }
}
