package com.uzuu.diuchat.feature.location.domain.usecase

import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository

class StartLiveLocationUseCase(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        durationMinutes: Int,
    ): Result<LiveLocationSession> = locationRepository.startLiveSharing(conversationId, durationMinutes)
}
