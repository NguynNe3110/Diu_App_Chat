package com.uzuu.diuchat.feature.location.domain.repository

import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<LocationSnapshot>

    fun getLiveLocationSession(sessionId: String): Flow<LiveLocationSession?>

    suspend fun startLiveSharing(
        conversationId: String,
        durationMinutes: Int,
    ): Result<LiveLocationSession>

    suspend fun stopLiveSharing(sessionId: String): Result<Unit>

    suspend fun updateLiveLocation(
        sessionId: String,
        location: LocationSnapshot,
    ): Result<Unit>
}
