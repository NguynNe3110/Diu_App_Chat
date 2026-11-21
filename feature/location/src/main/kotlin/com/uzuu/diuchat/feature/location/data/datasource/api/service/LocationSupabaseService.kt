package com.uzuu.diuchat.feature.location.data.datasource.api.service

import com.uzuu.diuchat.feature.location.data.datasource.api.model.LiveLocationDto
import com.uzuu.diuchat.feature.location.data.datasource.api.response.LiveLocationResponse

interface LocationSupabaseService {
    suspend fun upsertLiveSession(session: LiveLocationDto): Result<LiveLocationResponse>

    suspend fun fetchLiveSession(sessionId: String): Result<LiveLocationResponse>

    suspend fun stopLiveSession(sessionId: String): Result<Unit>

    suspend fun updateLiveCoordinates(session: LiveLocationDto): Result<Unit>
}
