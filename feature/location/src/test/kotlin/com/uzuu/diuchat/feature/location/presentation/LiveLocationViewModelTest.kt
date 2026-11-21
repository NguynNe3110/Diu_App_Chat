package com.uzuu.diuchat.feature.location.presentation

import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import com.uzuu.diuchat.feature.location.domain.usecase.ObserveLiveLocationUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.StopLiveLocationUseCase
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class LiveLocationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockObserveLiveLocationUseCase: ObserveLiveLocationUseCase = mockk()
    private val mockStopLiveLocationUseCase: StopLiveLocationUseCase = mockk()

    private lateinit var sut: LiveLocationViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sut = LiveLocationViewModel(
            observeLiveLocationUseCase = mockObserveLiveLocationUseCase,
            stopLiveLocationUseCase = mockStopLiveLocationUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeSession updates state with session and begins ticker`() = runTest {
        val now = Instant.now()
        val session = LiveLocationSession(
            sessionId = "session-123",
            conversationId = "conv-1",
            userId = "user-1",
            userName = "Tôi",
            durationMinutes = 15,
            startedAt = now,
            expiresAt = now.plusSeconds(900),
            isActive = true,
            lastKnownLocation = LocationSnapshot(21.0, 105.0, 10f),
        )
        every { mockObserveLiveLocationUseCase("session-123") } returns flowOf(session)

        sut.observeSession("session-123")

        val state = sut.uiStateFlow.first()
        assertEquals(session, state.session)
        assertTrue(state.isSharingActive)
    }

    @Test
    fun `stopSharing triggers use case and updates isSharingActive to false`() = runTest {
        coEvery { mockStopLiveLocationUseCase("session-123") } returns Result.success(Unit)

        sut.stopSharing("session-123")

        val state = sut.uiStateFlow.first()
        assertFalse(state.isSharingActive)
    }
}
