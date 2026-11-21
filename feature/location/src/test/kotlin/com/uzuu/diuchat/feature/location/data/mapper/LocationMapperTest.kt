package com.uzuu.diuchat.feature.location.data.mapper

import com.uzuu.diuchat.feature.location.data.datasource.api.model.LiveLocationDto
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LocationMapperTest {
    private val mapper = LocationMapper()

    @Test
    fun `toDto and toDomain roundtrip LiveLocationSession with snapshot`() {
        val startedAt = Instant.parse("2026-11-22T00:00:00Z")
        val expiresAt = Instant.parse("2026-11-22T00:15:00Z")
        val capturedAt = Instant.parse("2026-11-22T00:01:00Z")
        val domain = LiveLocationSession(
            sessionId = "sess-1",
            conversationId = "conv-1",
            userId = "user-1",
            userName = "Minh",
            durationMinutes = 15,
            startedAt = startedAt,
            expiresAt = expiresAt,
            isActive = true,
            lastKnownLocation = LocationSnapshot(
                latitude = 21.0285,
                longitude = 105.8542,
                accuracyMeters = 12f,
                address = "Hà Nội",
                capturedAt = capturedAt,
            ),
        )

        val dto = mapper.toDto(domain)
        assertEquals("sess-1", dto.sessionId)
        assertEquals(21.0285, dto.latitude)
        assertEquals("Hà Nội", dto.address)

        val roundtrip = mapper.toDomain(dto)
        assertEquals(domain, roundtrip)
    }

    @Test
    fun `toDomain produces null snapshot when coordinates are missing`() {
        val dto = LiveLocationDto(
            sessionId = "sess-2",
            conversationId = "conv-2",
            userId = "user-2",
            userName = "Thảo",
            durationMinutes = 30,
            startedAtIso = "2026-11-22T00:00:00Z",
            expiresAtIso = "2026-11-22T00:30:00Z",
            isActive = false,
        )

        val domain = mapper.toDomain(dto)
        assertEquals("sess-2", domain.sessionId)
        assertEquals(false, domain.isActive)
        assertNull(domain.lastKnownLocation)
    }
}
