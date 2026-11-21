package com.uzuu.diuchat.feature.media.presentation.storage

import com.uzuu.diuchat.feature.media.domain.model.StorageStats
import com.uzuu.diuchat.feature.media.domain.usecase.ClearCacheUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetStorageStatsUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class StorageViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockClearCacheUseCase: ClearCacheUseCase = mockk()
    private val mockGetStorageStatsUseCase: GetStorageStatsUseCase = mockk {
        every { this@mockk() } returns flowOf(
            StorageStats(
                totalStorageBytes = 1_000_000L,
                photosBytes = 600_000L,
                voiceBytes = 200_000L,
                cacheBytes = 200_000L,
            ),
        )
    }

    private lateinit var sut: StorageViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sut = StorageViewModel(mockClearCacheUseCase, mockGetStorageStatsUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads storage stats`() = runTest {
        val state = sut.uiStateFlow.first()
        assertEquals(1_000_000L, state.stats.totalStorageBytes)
        assertEquals(200_000L, state.stats.cacheBytes)
        assertTrue(state.autoDownloadWifi)
        assertTrue(state.autoDownloadVoice)
        assertFalse(state.keepOriginalQuality)
    }

    @Test
    fun `toggle switches updates settings state`() = runTest {
        sut.onToggleAutoDownloadWifi(false)
        sut.onToggleAutoDownloadVoice(false)
        sut.onToggleKeepOriginalQuality(true)

        val state = sut.uiStateFlow.first()
        assertFalse(state.autoDownloadWifi)
        assertFalse(state.autoDownloadVoice)
        assertTrue(state.keepOriginalQuality)
    }

    @Test
    fun `confirm clear cache invokes use case and sets success message`() = runTest {
        coEvery { mockClearCacheUseCase() } returns Result.success(200_000L)

        sut.onConfirmClearCache()

        val state = sut.uiStateFlow.value
        assertFalse(state.showClearDialog)
        assertFalse(state.isClearing)
        assertTrue(state.lastClearedMessage?.contains("Đã dọn dẹp") == true)
    }
}
