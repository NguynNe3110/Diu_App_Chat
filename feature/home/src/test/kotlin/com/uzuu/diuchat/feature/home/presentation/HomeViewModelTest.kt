package com.uzuu.diuchat.feature.home.presentation

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeViewModelTest {
    private val sut = HomeViewModel()

    @Test
    fun `initial state loads stars and recent chats`() = runTest {
        val state = sut.uiStateFlow.first()

        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertTrue(state.stars.isNotEmpty())
        assertTrue(state.recentChats.isNotEmpty())
    }

    @Test
    fun `clear data action transitions to empty state`() = runTest {
        sut.onClearData()

        val state = sut.uiStateFlow.first()
        assertTrue(state.isEmpty)
        assertEquals(0, state.stars.size)
        assertEquals(0, state.recentChats.size)
    }

    @Test
    fun `set data action updates stars and quiet mode`() = runTest {
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
        sut.onDismissQuietBanner(dismiss = true)

        val state = sut.uiStateFlow.first()
        assertFalse(state.isQuietModeActive)
    }
}
