package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMonthlyMoodCheckinsUseCaseTest {

    private val mockJournalRepository: JournalRepository = mockk()
    private val sut = GetMonthlyMoodCheckinsUseCase(mockJournalRepository)

    @Test
    fun `invoke delegates to repository with month and year`() = runTest {
        val expected = listOf(
            DailyMoodCheckin(
                dateIso = "2026-11-24",
                dayNumber = 24,
                mood = JournalMoodType.BINH_YEN,
                privateNote = "Thư thái",
            ),
        )
        every { mockJournalRepository.getMonthlyMoodCheckins(11, 2026) } returns flowOf(expected)

        val result = sut(11, 2026).first()

        assertEquals(expected, result)
    }
}
