package com.uzuu.diuchat.feature.location.domain.usecase

import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StartLiveLocationUseCaseTest {

    private val mockLocationRepository: LocationRepository = mockk()
    private val sut = StartLiveLocationUseCase(mockLocationRepository)

    @Test
    fun `invoke delegates to repository startLiveSharing`() = runTest {
        val session = LiveLocationSession(
            sessionId = "session-1",
            conversationId = "conv-1",
            userId = "user-1",
            userName = "Tôi",
            durationMinutes = 60,
        )
        coEvery { mockLocationRepository.startLiveSharing("conv-1", 60) } returns Result.success(session)

        val result = sut("conv-1", 60)

        assertTrue(result.isSuccess)
        assertEquals(session, result.getOrNull())
    }
}
