package com.uzuu.diuchat.feature.media.presentation.storage

import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.model.ConversationStorage
import com.uzuu.diuchat.feature.media.domain.model.StorageStats
import com.uzuu.diuchat.feature.media.domain.usecase.ClearCacheUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.ClearConversationMediaUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetAutoDownloadPolicyUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetStorageStatsUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.UpdateAutoDownloadPolicyUseCase
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
    private val mockClearConversationMediaUseCase: ClearConversationMediaUseCase = mockk()
    private val mockGetAutoDownloadPolicyUseCase: GetAutoDownloadPolicyUseCase = mockk {
        every { this@mockk() } returns flowOf(AutoDownloadPolicy.WIFI_ONLY)
    }
    private val mockGetStorageStatsUseCase: GetStorageStatsUseCase = mockk {
        every { this@mockk() } returns flowOf(
            StorageStats(
                totalStorageBytes = 1_000_000L,
                photosBytes = 600_000L,
                voiceBytes = 200_000L,
                cacheBytes = 200_000L,
                conversations = listOf(
                    ConversationStorage(
                        conversationId = "minh",
                        contactName = "Minh",
                        initials = "M",
                        moodColor = AppPalette.MoodThuong,
                        totalBytes = 400_000L,
                        photoCount = 2,
                        voiceCount = 1,
                    ),
                ),
            ),
        )
    }
    private val mockUpdateAutoDownloadPolicyUseCase: UpdateAutoDownloadPolicyUseCase = mockk()

    private lateinit var sut: StorageViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sut = StorageViewModel(
            clearCacheUseCase = mockClearCacheUseCase,
            clearConversationMediaUseCase = mockClearConversationMediaUseCase,
            getAutoDownloadPolicyUseCase = mockGetAutoDownloadPolicyUseCase,
            getStorageStatsUseCase = mockGetStorageStatsUseCase,
            updateAutoDownloadPolicyUseCase = mockUpdateAutoDownloadPolicyUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads storage stats and policy`() = runTest {
        val state = sut.uiStateFlow.first()
        assertEquals(1_000_000L, state.stats.totalStorageBytes)
        assertEquals(200_000L, state.stats.cacheBytes)
        assertEquals(AutoDownloadPolicy.WIFI_ONLY, state.autoDownloadPolicy)
        assertTrue(state.autoDownloadWifi)
        assertTrue(state.autoDownloadVoice)
        assertFalse(state.keepOriginalQuality)
    }

    @Test
    fun `toggle switches updates settings state`() = runTest {
        coEvery { mockUpdateAutoDownloadPolicyUseCase(any()) } just runs

        sut.onToggleAutoDownloadWifi(false)
        sut.onToggleAutoDownloadVoice(false)
        sut.onToggleKeepOriginalQuality(true)

        val state = sut.uiStateFlow.first()
        assertFalse(state.autoDownloadWifi)
        assertFalse(state.autoDownloadVoice)
        assertTrue(state.keepOriginalQuality)
        assertEquals(AutoDownloadPolicy.MANUAL, state.autoDownloadPolicy)
    }

    @Test
    fun `onSetAutoDownloadPolicy updates policy via usecase and state`() = runTest {
        coEvery { mockUpdateAutoDownloadPolicyUseCase(AutoDownloadPolicy.CELLULAR_AND_WIFI) } just runs

        sut.onSetAutoDownloadPolicy(AutoDownloadPolicy.CELLULAR_AND_WIFI)

        val state = sut.uiStateFlow.value
        assertEquals(AutoDownloadPolicy.CELLULAR_AND_WIFI, state.autoDownloadPolicy)
        coVerify(exactly = 1) { mockUpdateAutoDownloadPolicyUseCase(AutoDownloadPolicy.CELLULAR_AND_WIFI) }
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

    @Test
    fun `confirm clear conversation media invokes use case and sets success message`() = runTest {
        val targetConv = ConversationStorage(
            conversationId = "minh",
            contactName = "Minh",
            initials = "M",
            moodColor = AppPalette.MoodThuong,
            totalBytes = 400_000L,
            photoCount = 2,
            voiceCount = 1,
        )
        sut.onSelectConversationToClear(targetConv)
        assertTrue(sut.uiStateFlow.value.showConversationClearDialog)
        assertEquals(targetConv, sut.uiStateFlow.value.selectedConversationToClear)

        coEvery { mockClearConversationMediaUseCase("minh") } returns Result.success(400_000L)

        sut.onConfirmClearConversationMedia()

        val state = sut.uiStateFlow.value
        assertFalse(state.showConversationClearDialog)
        assertFalse(state.isClearingConversation)
        assertTrue(state.lastClearedMessage?.contains("Đã xóa") == true)
        coVerify(exactly = 1) { mockClearConversationMediaUseCase("minh") }
    }
}
