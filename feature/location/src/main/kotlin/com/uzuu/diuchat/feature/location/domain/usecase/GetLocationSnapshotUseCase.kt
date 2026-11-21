package com.uzuu.diuchat.feature.location.domain.usecase

import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository

class GetLocationSnapshotUseCase(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): Result<LocationSnapshot> = locationRepository.getCurrentLocation()
}
