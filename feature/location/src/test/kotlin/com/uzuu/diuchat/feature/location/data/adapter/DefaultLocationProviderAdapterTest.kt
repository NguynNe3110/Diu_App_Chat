package com.uzuu.diuchat.feature.location.data.adapter

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DefaultLocationProviderAdapterTest {

    private val sut = DefaultLocationProviderAdapter()

    @Test
    fun `requestSingleFix returns default snapshot`() = runTest {
        val result = sut.requestSingleFix()

        assertTrue(result.isSuccess)
        val snapshot = result.getOrNull()
        assertEquals(DefaultLocationProviderAdapter.DEFAULT_LATITUDE, snapshot?.latitude)
        assertEquals(DefaultLocationProviderAdapter.DEFAULT_LONGITUDE, snapshot?.longitude)
        assertEquals(DefaultLocationProviderAdapter.DEFAULT_ACCURACY, snapshot?.accuracyMeters)
    }

    @Test
    fun `getAdaptiveSamplingIntervalMs returns stationary interval when speed is low`() {
        val interval = sut.getAdaptiveSamplingIntervalMs(speedMps = 0.2f, accuracyMeters = 10f)
        assertEquals(DefaultLocationProviderAdapter.INTERVAL_STATIONARY_MS, interval)
    }

    @Test
    fun `getAdaptiveSamplingIntervalMs returns walking interval when speed is moderate`() {
        val interval = sut.getAdaptiveSamplingIntervalMs(speedMps = 1.5f, accuracyMeters = 10f)
        assertEquals(DefaultLocationProviderAdapter.INTERVAL_WALKING_MS, interval)
    }

    @Test
    fun `getAdaptiveSamplingIntervalMs returns fast transit interval when speed is high`() {
        val interval = sut.getAdaptiveSamplingIntervalMs(speedMps = 8.0f, accuracyMeters = 10f)
        assertEquals(DefaultLocationProviderAdapter.INTERVAL_FAST_TRANSIT_MS, interval)
    }

    @Test
    fun `getAdaptiveSamplingIntervalMs returns poor accuracy interval when accuracy is low`() {
        val interval = sut.getAdaptiveSamplingIntervalMs(speedMps = 1.0f, accuracyMeters = 60f)
        assertEquals(DefaultLocationProviderAdapter.INTERVAL_POOR_ACCURACY_MS, interval)
    }
}
