package com.uzuu.diuchat.feature.location.domain.model

import java.time.Instant

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val address: String? = null,
    val capturedAt: Instant = Instant.now(),
)
