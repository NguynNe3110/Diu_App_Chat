package com.uzuu.diuchat.feature.location.data.repository

import com.uzuu.diuchat.feature.location.data.adapter.LocationProviderAdapter
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationRepositoryImpl(
    private val locationProviderAdapter: LocationProviderAdapter,
) : LocationRepository {

    private val sessions = ConcurrentHashMap<String, LiveLocationSession>()
    private val sessionFlows = ConcurrentHashMap<String, MutableStateFlow<LiveLocationSession?>>()

    override suspend fun getCurrentLocation(): Result<LocationSnapshot> = locationProviderAdapter.requestSingleFix()

    override fun getLiveLocationSession(sessionId: String): Flow<LiveLocationSession?> {
        val flow = sessionFlows.computeIfAbsent(sessionId) {
            MutableStateFlow(sessions[sessionId])
        }
        return flow.asStateFlow()
    }

    override suspend fun startLiveSharing(
        conversationId: String,
        durationMinutes: Int,
    ): Result<LiveLocationSession> {
        val initialFixResult = locationProviderAdapter.requestSingleFix()
        val initialFix = initialFixResult.getOrNull()

        val now = Instant.now()
        val sessionId = UUID.randomUUID().toString()
        val session = LiveLocationSession(
            sessionId = sessionId,
            conversationId = conversationId,
            userId = "user_self",
            userName = "Tôi",
            durationMinutes = durationMinutes,
            startedAt = now,
            expiresAt = now.plusSeconds(durationMinutes * SECONDS_PER_MINUTE),
            isActive = true,
            lastKnownLocation = initialFix,
        )

        sessions[sessionId] = session
        val flow = sessionFlows.computeIfAbsent(sessionId) { MutableStateFlow(null) }
        flow.value = session

        return Result.success(session)
    }

    override suspend fun stopLiveSharing(sessionId: String): Result<Unit> {
        val existing = sessions[sessionId]
            ?: return Result.failure(IllegalArgumentException("Session $sessionId không tồn tại"))

        val stopped = existing.copy(isActive = false)
        sessions[sessionId] = stopped
        sessionFlows[sessionId]?.value = stopped

        return Result.success(Unit)
    }

    override suspend fun updateLiveLocation(
        sessionId: String,
        location: LocationSnapshot,
    ): Result<Unit> {
        val existing = sessions[sessionId]
            ?: return Result.failure(IllegalArgumentException("Session $sessionId không tồn tại"))

        if (!existing.isActive || existing.isExpired) {
            val deactivated = existing.copy(isActive = false)
            sessions[sessionId] = deactivated
            sessionFlows[sessionId]?.value = deactivated
            return Result.failure(IllegalStateException("Phiên chia sẻ vị trí đã kết thúc hoặc hết hạn."))
        }

        val updated = existing.copy(lastKnownLocation = location)
        sessions[sessionId] = updated
        sessionFlows[sessionId]?.value = updated

        return Result.success(Unit)
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60L
    }
}
