package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository

class ClearCacheUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(): Result<Long> = mediaRepository.clearCache()
}
