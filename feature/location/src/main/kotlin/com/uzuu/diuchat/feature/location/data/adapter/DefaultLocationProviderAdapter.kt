package com.uzuu.diuchat.feature.location.data.adapter

import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import java.time.Instant

class DefaultLocationProviderAdapter : LocationProviderAdapter {

    override suspend fun requestSingleFix(): Result<LocationSnapshot> {
        val snapshot = LocationSnapshot(
            latitude = DEFAULT_LATITUDE,
            longitude = DEFAULT_LONGITUDE,
            accuracyMeters = DEFAULT_ACCURACY,
            address = DEFAULT_ADDRESS,
            capturedAt = Instant.now(),
        )
        return Result.success(snapshot)
    }

    override fun getAdaptiveSamplingIntervalMs(
        speedMps: Float,
        accuracyMeters: Float,
    ): Long {
        if (accuracyMeters > ACCURACY_POOR_THRESHOLD_METERS) {
            return INTERVAL_POOR_ACCURACY_MS
        }
        return when {
            speedMps < SPEED_STATIONARY_THRESHOLD_MPS -> INTERVAL_STATIONARY_MS
            speedMps <= SPEED_WALKING_THRESHOLD_MPS -> INTERVAL_WALKING_MS
            else -> INTERVAL_FAST_TRANSIT_MS
        }
    }

    companion object {
        const val SPEED_STATIONARY_THRESHOLD_MPS = 0.5f
        const val SPEED_WALKING_THRESHOLD_MPS = 4.0f
        const val ACCURACY_POOR_THRESHOLD_METERS = 50.0f

        const val INTERVAL_STATIONARY_MS = 30_000L
        const val INTERVAL_WALKING_MS = 10_000L
        const val INTERVAL_FAST_TRANSIT_MS = 3_000L
        const val INTERVAL_POOR_ACCURACY_MS = 15_000L

        const val DEFAULT_LATITUDE = 21.0285
        const val DEFAULT_LONGITUDE = 105.8542
        const val DEFAULT_ACCURACY = 12.0f
        const val DEFAULT_ADDRESS = "Hồ Hoàn Kiếm, Hoàn Kiếm, Hà Nội"
    }
}
