package com.uzuu.diuchat.feature.media.presentation.vault

import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem
import com.uzuu.diuchat.feature.media.domain.usecase.GetVaultMediaUseCase
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class MediaVaultViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockGetVaultMediaUseCase: GetVaultMediaUseCase = mockk {
        every { this@mockk(any()) } returns flowOf(
            listOf(
                MediaVaultItem(id = "p1", conversationId = "c1", type = MediaType.PHOTO, caption = "Photo 1"),
                MediaVaultItem(id = "v1", conversationId = "c1", type = MediaType.VOICE, caption = "Voice 1"),
                MediaVaultItem(id = "l1", conversationId = "c1", type = MediaType.LETTER, caption = "Letter 1"),
            ),
        )
    }

    private lateinit var sut: MediaVaultViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sut = MediaVaultViewModel(mockGetVaultMediaUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads all items`() = runTest {
        val state = sut.uiStateFlow.first()
        assertEquals(MediaType.ALL, state.selectedType)
        assertEquals(3, state.allItems.size)
        assertEquals(3, state.filteredItems.size)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `selectType filters items properly`() = runTest {
        sut.onSelectType(MediaType.PHOTO)
        val state = sut.uiStateFlow.first()

        assertEquals(MediaType.PHOTO, state.selectedType)
        assertEquals(1, state.filteredItems.size)
        assertEquals("p1", state.filteredItems.first().id)
    }

    @Test
    fun `togglePlayVoice updates currentlyPlayingVoiceId`() = runTest {
        sut.onTogglePlayVoice("v1")
        val state = sut.uiStateFlow.first()
        assertEquals("v1", state.currentlyPlayingVoiceId)

        sut.onTogglePlayVoice("v1")
        val state2 = sut.uiStateFlow.first()
        assertNull(state2.currentlyPlayingVoiceId)
    }
}
