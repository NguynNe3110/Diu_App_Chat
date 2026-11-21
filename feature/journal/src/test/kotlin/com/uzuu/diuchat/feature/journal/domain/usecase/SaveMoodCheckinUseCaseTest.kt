package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SaveMoodCheckinUseCaseTest {

    private val mockJournalRepository: JournalRepository = mockk()
    private val sut = SaveMoodCheckinUseCase(mockJournalRepository)

    @Test
    fun `invoke delegates to repository saveMoodCheckin`() = runTest {
        val checkin = DailyMoodCheckin(
            dateIso = "2026-11-24",
            dayNumber = 24,
            mood = JournalMoodType.THUONG,
            privateNote = "Hôm nay vui",
        )
        coEvery { mockJournalRepository.saveMoodCheckin(checkin) } returns Result.success(Unit)

        val result = sut(checkin)

        assertEquals(Result.success(Unit), result)
    }
}
