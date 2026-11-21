package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.FavouriteMemory
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow

class GetFavouritesUseCase(
    private val journalRepository: JournalRepository,
) {
    operator fun invoke(): Flow<List<FavouriteMemory>> = journalRepository.getFavourites()
}
