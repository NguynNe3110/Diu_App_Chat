package com.uzuu.diuchat.feature.home.presentation

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import com.uzuu.diuchat.feature.home.domain.usecase.GetHomeStarsUseCase
import com.uzuu.diuchat.feature.home.domain.usecase.GetRecentChatsUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class HomeViewModelTest {
    private val getHomeStarsUseCase: GetHomeStarsUseCase = mockk()
    private val getRecentChatsUseCase: GetRecentChatsUseCase = mockk()

    private val defaultStars =
        listOf(
            StarPerson("minh", "Minh", "M", Color.Red),
            StarPerson("nha", "Nhà mình", "N", Color.Yellow, memberCount = 3),
        )
    private val defaultChats =
        listOf(
            RecentChat("minh", "Minh", "M", Color.Red, "Hello", "21:14"),
        )

    private lateinit var sut: HomeViewModel

    @BeforeEach
    fun setUp() {
        every { getHomeStarsUseCase(any()) } returns flowOf(defaultStars)
        every { getRecentChatsUseCase() } returns flowOf(defaultChats)

        sut = HomeViewModel(getHomeStarsUseCase, getRecentChatsUseCase)
    }

    @Test
    fun `initial state loads stars and recent chats`() = runTest {
        advanceUntilIdle()
        val state = sut.uiStateFlow.first()

        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals(2, state.stars.size)
        assertEquals(1, state.recentChats.size)
    }

    @Test
    fun `clear data action transitions to empty state`() = runTest {
        advanceUntilIdle()
        sut.onClearData()

        val state = sut.uiStateFlow.first()
        assertTrue(state.isEmpty)
        assertEquals(0, state.stars.size)
        assertEquals(0, state.recentChats.size)
    }

    @Test
    fun `set data action updates stars and quiet mode`() = runTest {
        advanceUntilIdle()
        val customStars =
            listOf(
                StarPerson(
                    id = "test-1",
                    name = "Test",
                    initials = "T",
                    moodColor = Color.Red,
                    isQuiet = true,
                ),
            )
        val customChats =
            listOf(
                RecentChat(
                    id = "chat-1",
                    contactName = "Test",
                    initials = "T",
                    moodColor = Color.Red,
                    lastMessage = "Hello",
                    timestamp = "10:00",
                    unreadCount = 1,
                ),
            )

        sut.onSetData(customStars, customChats, isQuietModeActive = true)

        val state = sut.uiStateFlow.first()
        assertEquals(1, state.stars.size)
        assertEquals(1, state.recentChats.size)
        assertTrue(state.isQuietModeActive)
    }

    @Test
    fun `dismiss quiet banner updates state`() = runTest {
        advanceUntilIdle()
        sut.onDismissQuietBanner(dismiss = true)

        val state = sut.uiStateFlow.first()
        assertFalse(state.isQuietModeActive)
    }
}
