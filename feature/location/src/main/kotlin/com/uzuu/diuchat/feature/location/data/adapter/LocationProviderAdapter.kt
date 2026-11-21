package com.uzuu.diuchat.feature.location.data.adapter

import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot

interface LocationProviderAdapter {
    suspend fun requestSingleFix(): Result<LocationSnapshot>

    fun getAdaptiveSamplingIntervalMs(
        speedMps: Float,
        accuracyMeters: Float,
    ): Long
}
