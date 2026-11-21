package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository

class SaveMoodCheckinUseCase(
    private val journalRepository: JournalRepository,
) {
    suspend operator fun invoke(checkin: DailyMoodCheckin): Result<Unit> = journalRepository.saveMoodCheckin(checkin)
}
