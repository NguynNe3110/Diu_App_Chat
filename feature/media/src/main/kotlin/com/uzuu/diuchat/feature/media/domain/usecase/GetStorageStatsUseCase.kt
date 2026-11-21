package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.model.StorageStats
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetStorageStatsUseCase(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(): Flow<StorageStats> =
        mediaRepository.getStorageStats()
}
