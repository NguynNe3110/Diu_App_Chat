package com.uzuu.diuchat.feature.location.data.repository

import com.uzuu.diuchat.feature.location.data.adapter.LocationProviderAdapter
import com.uzuu.diuchat.feature.location.data.datasource.api.service.LocationSupabaseService
import com.uzuu.diuchat.feature.location.data.mapper.LocationMapper
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocationRepositoryImplTest {

    private val mockLocationProviderAdapter: LocationProviderAdapter = mockk()
    private val mockSupabaseService: LocationSupabaseService = mockk {
        coEvery { fetchLiveSession(any()) } returns Result.failure(IllegalStateException("offline"))
        coEvery { upsertLiveSession(any()) } returns Result.failure(IllegalStateException("offline"))
        coEvery { stopLiveSession(any()) } returns Result.failure(IllegalStateException("offline"))
        coEvery {
            updateLiveCoordinates(any())
        } returns Result.failure(IllegalStateException("offline"))
    }
    private val mapper = LocationMapper()
    private val sut = LocationRepositoryImpl(
        locationProviderAdapter = mockLocationProviderAdapter,
        supabaseService = mockSupabaseService,
        mapper = mapper,
    )

    @Test
    fun `getCurrentLocation delegates to adapter`() = runTest {
        val snapshot = LocationSnapshot(latitude = 21.0, longitude = 105.0, accuracyMeters = 10f)
        coEvery { mockLocationProviderAdapter.requestSingleFix() } returns Result.success(snapshot)

        val result = sut.getCurrentLocation()

        assertTrue(result.isSuccess)
        assertEquals(snapshot, result.getOrNull())
    }

    @Test
    fun `startLiveSharing creates active session and emits to flow`() = runTest {
        val snapshot = LocationSnapshot(latitude = 21.0, longitude = 105.0, accuracyMeters = 10f)
        coEvery { mockLocationProviderAdapter.requestSingleFix() } returns Result.success(snapshot)

        val result = sut.startLiveSharing(conversationId = "conv-123", durationMinutes = 15)

        assertTrue(result.isSuccess)
        val session = result.getOrNull()
        assertNotNull(session)
        assertTrue(session!!.isActive)
        assertEquals("conv-123", session.conversationId)
        assertEquals(snapshot, session.lastKnownLocation)

        val observed = sut.getLiveLocationSession(session.sessionId).first()
        assertEquals(session, observed)
    }

    @Test
    fun `stopLiveSharing marks session inactive and emits to flow`() = runTest {
        val snapshot = LocationSnapshot(latitude = 21.0, longitude = 105.0, accuracyMeters = 10f)
        coEvery { mockLocationProviderAdapter.requestSingleFix() } returns Result.success(snapshot)

        val startResult = sut.startLiveSharing("conv-1", 15)
        val sessionId = startResult.getOrNull()!!.sessionId

        val stopResult = sut.stopLiveSharing(sessionId)
        assertTrue(stopResult.isSuccess)

        val observed = sut.getLiveLocationSession(sessionId).first()
        assertNotNull(observed)
        assertFalse(observed!!.isActive)
    }

    @Test
    fun `updateLiveLocation updates lastKnownLocation when session is active`() = runTest {
        val snapshot1 = LocationSnapshot(latitude = 21.0, longitude = 105.0, accuracyMeters = 10f)
        val snapshot2 = LocationSnapshot(latitude = 21.001, longitude = 105.001, accuracyMeters = 5f)
        coEvery { mockLocationProviderAdapter.requestSingleFix() } returns Result.success(snapshot1)

        val startResult = sut.startLiveSharing("conv-1", 15)
        val sessionId = startResult.getOrNull()!!.sessionId

        val updateResult = sut.updateLiveLocation(sessionId, snapshot2)
        assertTrue(updateResult.isSuccess)

        val observed = sut.getLiveLocationSession(sessionId).first()
        assertEquals(snapshot2, observed?.lastKnownLocation)
    }

    @Test
    fun `updateLiveLocation fails when session is stopped`() = runTest {
        val snapshot1 = LocationSnapshot(latitude = 21.0, longitude = 105.0, accuracyMeters = 10f)
        val snapshot2 = LocationSnapshot(latitude = 21.001, longitude = 105.001, accuracyMeters = 5f)
        coEvery { mockLocationProviderAdapter.requestSingleFix() } returns Result.success(snapshot1)

        val startResult = sut.startLiveSharing("conv-1", 15)
        val sessionId = startResult.getOrNull()!!.sessionId
        sut.stopLiveSharing(sessionId)

        val updateResult = sut.updateLiveLocation(sessionId, snapshot2)
        assertTrue(updateResult.isFailure)
    }
}
