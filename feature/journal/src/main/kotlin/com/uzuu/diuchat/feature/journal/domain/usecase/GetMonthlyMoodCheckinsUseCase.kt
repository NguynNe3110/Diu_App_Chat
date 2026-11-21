package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow

class GetMonthlyMoodCheckinsUseCase(
    private val journalRepository: JournalRepository,
) {
    operator fun invoke(
        month: Int,
        year: Int,
    ): Flow<List<DailyMoodCheckin>> = journalRepository.getMonthlyMoodCheckins(month = month, year = year)
}
