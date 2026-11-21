package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.SearchResultItem
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SearchMessagesUseCaseTest {

    private val mockJournalRepository: JournalRepository = mockk()
    private val sut = SearchMessagesUseCase(mockJournalRepository)

    @Test
    fun `invoke delegates to repository searchMessages`() = runTest {
        val expected = listOf(
            SearchResultItem(
                id = "1",
                conversationId = "c1",
                conversationName = "Minh",
                senderName = "Minh",
                snippet = "Bánh flan ngon nè",
                timestamp = "21:00",
                mood = JournalMoodType.THUONG,
            ),
        )
        every {
            mockJournalRepository.searchMessages(
                query = "flan",
                mood = JournalMoodType.THUONG,
                conversationId = "c1",
            )
        } returns flowOf(expected)

        val result = sut(
            query = "flan",
            mood = JournalMoodType.THUONG,
            conversationId = "c1",
        ).first()

        assertEquals(expected, result)
    }
}
