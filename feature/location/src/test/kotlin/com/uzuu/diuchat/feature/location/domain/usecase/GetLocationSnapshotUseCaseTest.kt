package com.uzuu.diuchat.feature.location.domain.usecase

import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetLocationSnapshotUseCaseTest {

    private val mockLocationRepository: LocationRepository = mockk()
    private val sut = GetLocationSnapshotUseCase(mockLocationRepository)

    @Test
    fun `invoke returns location snapshot on success`() = runTest {
        val expected = LocationSnapshot(
            latitude = 21.0285,
            longitude = 105.8542,
            accuracyMeters = 10f,
            address = "Hà Nội",
        )
        coEvery { mockLocationRepository.getCurrentLocation() } returns Result.success(expected)

        val result = sut()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val exception = RuntimeException("Location unavailable")
        coEvery { mockLocationRepository.getCurrentLocation() } returns Result.failure(exception)

        val result = sut()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
