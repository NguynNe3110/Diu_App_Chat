package com.uzuu.diuchat.feature.location.domain.usecase

import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class ObserveLiveLocationUseCase(
    private val locationRepository: LocationRepository,
) {
    operator fun invoke(sessionId: String): Flow<LiveLocationSession?> = locationRepository.getLiveLocationSession(sessionId)
}
