package com.uzuu.diuchat.feature.location.presentation

import com.uzuu.diuchat.feature.location.domain.model.LiveDuration
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import com.uzuu.diuchat.feature.location.domain.usecase.GetLocationSnapshotUseCase
import com.uzuu.diuchat.feature.location.domain.usecase.StartLiveLocationUseCase
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class LocationPickerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockLocationSnapshot = LocationSnapshot(
        latitude = 21.0285,
        longitude = 105.8542,
        accuracyMeters = 10f,
        address = "Hà Nội",
    )

    private val mockGetLocationSnapshotUseCase: GetLocationSnapshotUseCase = mockk()
    private val mockStartLiveLocationUseCase: StartLiveLocationUseCase = mockk()

    private lateinit var sut: LocationPickerViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockGetLocationSnapshotUseCase() } returns Result.success(mockLocationSnapshot)
        sut = LocationPickerViewModel(
            getLocationSnapshotUseCase = mockGetLocationSnapshotUseCase,
            startLiveLocationUseCase = mockStartLiveLocationUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads current location`() = runTest {
        val state = sut.uiStateFlow.first()
        assertEquals(mockLocationSnapshot, state.currentLocation)
        assertFalse(state.isLoading)
    }

    @Test
    fun `toggleMode switches between static and live`() = runTest {
        sut.toggleMode(true)
        assertTrue(sut.uiStateFlow.first().isLiveMode)

        sut.toggleMode(false)
        assertFalse(sut.uiStateFlow.first().isLiveMode)
    }

    @Test
    fun `selectDuration updates selectedDuration in state`() = runTest {
        sut.selectDuration(LiveDuration.ONE_HOUR)
        assertEquals(LiveDuration.ONE_HOUR, sut.uiStateFlow.first().selectedDuration)

        sut.selectDuration(LiveDuration.EIGHT_HOURS)
        assertEquals(LiveDuration.EIGHT_HOURS, sut.uiStateFlow.first().selectedDuration)
    }

    @Test
    fun `startLiveSharing triggers use case and updates state`() = runTest {
        val expectedSession = LiveLocationSession(
            sessionId = "live-1",
            conversationId = "conv-1",
            userId = "user-1",
            userName = "Tôi",
            durationMinutes = 15,
        )
        coEvery { mockStartLiveLocationUseCase("conv-1", 15) } returns Result.success(expectedSession)

        var callbackSession: LiveLocationSession? = null
        sut.startLiveSharing("conv-1") { session ->
            callbackSession = session
        }

        val state = sut.uiStateFlow.first()
        assertEquals(expectedSession, state.createdSession)
        assertEquals(expectedSession, callbackSession)
    }
}
