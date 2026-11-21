package com.uzuu.diuchat.feature.journal.presentation.search

import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.SearchResultItem
import com.uzuu.diuchat.feature.journal.domain.usecase.SearchMessagesUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockSearchMessagesUseCase: SearchMessagesUseCase = mockk()

    private lateinit var sut: SearchViewModel

    private val testResults = listOf(
        SearchResultItem(
            id = "1",
            conversationId = "conv_minh",
            conversationName = "Minh",
            senderName = "Minh",
            snippet = "Bánh flan ngon nè",
            timestamp = "Hôm nay 21:10",
            mood = JournalMoodType.THUONG,
        ),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every {
            mockSearchMessagesUseCase(query = any(), mood = any(), conversationId = any())
        } returns flowOf(testResults)

        sut = SearchViewModel(
            searchMessagesUseCase = mockSearchMessagesUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load performs search with default state`() {
        val state = sut.uiStateFlow.value

        assertFalse(state.isLoading)
        assertEquals(testResults, state.results)
    }

    @Test
    fun `onQueryChange updates query and filters results`() {
        sut.onQueryChange("flan")

        assertEquals("flan", sut.uiStateFlow.value.query)
        assertEquals(testResults, sut.uiStateFlow.value.results)
    }

    @Test
    fun `onSelectMood updates mood filter in state`() {
        sut.onSelectMood(JournalMoodType.THUONG)

        assertEquals(JournalMoodType.THUONG, sut.uiStateFlow.value.selectedMood)

        // Toggle again to deselect
        sut.onSelectMood(JournalMoodType.THUONG)
        assertEquals(null, sut.uiStateFlow.value.selectedMood)
    }

    @Test
    fun `initScope sets conversationId and name`() {
        sut.initScope(conversationId = "conv_minh", conversationName = "Minh")

        val state = sut.uiStateFlow.value
        assertEquals("conv_minh", state.conversationId)
        assertEquals("Minh", state.conversationName)
    }
}
