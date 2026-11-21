package com.uzuu.diuchat.feature.location.domain.model

import java.time.Instant

data class LiveLocationSession(
    val sessionId: String,
    val conversationId: String,
    val userId: String,
    val userName: String,
    val durationMinutes: Int,
    val startedAt: Instant = Instant.now(),
    val expiresAt: Instant = startedAt.plusSeconds(durationMinutes * 60L),
    val isActive: Boolean = true,
    val lastKnownLocation: LocationSnapshot? = null,
) {
    val isExpired: Boolean
        get() = Instant.now().isAfter(expiresAt)

    val remainingSeconds: Long
        get() {
            val remaining = expiresAt.epochSecond - Instant.now().epochSecond
            return if (remaining > 0) remaining else 0L
        }
}
