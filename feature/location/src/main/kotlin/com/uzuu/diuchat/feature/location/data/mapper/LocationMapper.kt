package com.uzuu.diuchat.feature.location.data.mapper

import com.uzuu.diuchat.feature.location.data.datasource.api.model.LiveLocationDto
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import java.time.Instant

class LocationMapper {
    fun toDomain(dto: LiveLocationDto): LiveLocationSession {
        val startedAt = runCatching { Instant.parse(dto.startedAtIso) }.getOrElse { Instant.now() }
        val expiresAt = runCatching { Instant.parse(dto.expiresAtIso) }.getOrElse {
            startedAt.plusSeconds(dto.durationMinutes * SECONDS_PER_MINUTE)
        }
        val snapshot = if (dto.latitude != null && dto.longitude != null) {
            LocationSnapshot(
                latitude = dto.latitude,
                longitude = dto.longitude,
                accuracyMeters = dto.accuracyMeters ?: DEFAULT_ACCURACY_METERS,
                address = dto.address,
                capturedAt = dto.capturedAtIso?.let {
                    runCatching { Instant.parse(it) }.getOrNull()
                } ?: startedAt,
            )
        } else {
            null
        }

        return LiveLocationSession(
            sessionId = dto.sessionId,
            conversationId = dto.conversationId,
            userId = dto.userId,
            userName = dto.userName,
            durationMinutes = dto.durationMinutes,
            startedAt = startedAt,
            expiresAt = expiresAt,
            isActive = dto.isActive,
            lastKnownLocation = snapshot,
        )
    }

    fun toDto(domain: LiveLocationSession): LiveLocationDto = LiveLocationDto(
        sessionId = domain.sessionId,
        conversationId = domain.conversationId,
        userId = domain.userId,
        userName = domain.userName,
        durationMinutes = domain.durationMinutes,
        startedAtIso = domain.startedAt.toString(),
        expiresAtIso = domain.expiresAt.toString(),
        isActive = domain.isActive,
        latitude = domain.lastKnownLocation?.latitude,
        longitude = domain.lastKnownLocation?.longitude,
        accuracyMeters = domain.lastKnownLocation?.accuracyMeters,
        address = domain.lastKnownLocation?.address,
        capturedAtIso = domain.lastKnownLocation?.capturedAt?.toString(),
    )

    private companion object {
        private const val SECONDS_PER_MINUTE = 60L
        private const val DEFAULT_ACCURACY_METERS = 0f
    }
}
