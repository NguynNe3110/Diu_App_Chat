package com.uzuu.diuchat.feature.location.domain.usecase

import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StopLiveLocationUseCaseTest {

    private val mockLocationRepository: LocationRepository = mockk()
    private val sut = StopLiveLocationUseCase(mockLocationRepository)

    @Test
    fun `invoke delegates to repository stopLiveSharing`() = runTest {
        coEvery { mockLocationRepository.stopLiveSharing("session-1") } returns Result.success(Unit)

        val result = sut("session-1")

        assertTrue(result.isSuccess)
    }
}
