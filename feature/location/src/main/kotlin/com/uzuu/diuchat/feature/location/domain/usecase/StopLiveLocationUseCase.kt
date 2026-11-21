package com.uzuu.diuchat.feature.location.domain.usecase

import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository

class StopLiveLocationUseCase(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> = locationRepository.stopLiveSharing(sessionId)
}
