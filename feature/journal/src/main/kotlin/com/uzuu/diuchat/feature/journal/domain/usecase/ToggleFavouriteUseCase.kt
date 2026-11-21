package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository

class ToggleFavouriteUseCase(
    private val journalRepository: JournalRepository,
) {
    suspend operator fun invoke(memoryId: String): Result<Unit> = journalRepository.toggleFavourite(memoryId)
}
