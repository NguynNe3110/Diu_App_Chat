package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.MoodComparison
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow

class GetMoodComparisonUseCase(
    private val journalRepository: JournalRepository,
) {
    operator fun invoke(partnerId: String): Flow<MoodComparison> = journalRepository.getMoodComparison(partnerId)
}
