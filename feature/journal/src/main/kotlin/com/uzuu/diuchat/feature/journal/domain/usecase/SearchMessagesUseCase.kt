package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.SearchResultItem
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow

class SearchMessagesUseCase(
    private val journalRepository: JournalRepository,
) {
    operator fun invoke(
        query: String,
        mood: JournalMoodType? = null,
        conversationId: String? = null,
    ): Flow<List<SearchResultItem>> = journalRepository.searchMessages(
        query = query,
        mood = mood,
        conversationId = conversationId,
    )
}
