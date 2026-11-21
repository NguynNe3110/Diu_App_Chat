package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.MoodComparison
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMoodComparisonUseCaseTest {

    private val mockJournalRepository: JournalRepository = mockk()
    private val sut = GetMoodComparisonUseCase(mockJournalRepository)

    @Test
    fun `invoke delegates to repository with partnerId`() = runTest {
        val expected = MoodComparison(
            partnerId = "minh",
            partnerName = "Minh",
            matchingDaysCount = 6,
            primaryMood = JournalMoodType.BINH_YEN,
            myMoods = listOf(JournalMoodType.BINH_YEN),
            partnerMoods = listOf(JournalMoodType.BINH_YEN),
            summaryMessage = "Cùng bình yên 6 ngày",
        )
        every { mockJournalRepository.getMoodComparison("minh") } returns flowOf(expected)

        val result = sut("minh").first()

        assertEquals(expected, result)
    }
}
