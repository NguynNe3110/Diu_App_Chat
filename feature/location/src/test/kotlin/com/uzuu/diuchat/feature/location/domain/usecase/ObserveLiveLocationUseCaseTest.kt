package com.uzuu.diuchat.feature.location.domain.usecase

import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.repository.LocationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveLiveLocationUseCaseTest {

    private val mockLocationRepository: LocationRepository = mockk()
    private val sut = ObserveLiveLocationUseCase(mockLocationRepository)

    @Test
    fun `invoke delegates to repository getLiveLocationSession`() = runTest {
        val session = LiveLocationSession(
            sessionId = "session-1",
            conversationId = "conv-1",
            userId = "user-1",
            userName = "Tôi",
            durationMinutes = 15,
        )
        every { mockLocationRepository.getLiveLocationSession("session-1") } returns flowOf(session)

        val result = sut("session-1").first()

        assertEquals(session, result)
    }
}
