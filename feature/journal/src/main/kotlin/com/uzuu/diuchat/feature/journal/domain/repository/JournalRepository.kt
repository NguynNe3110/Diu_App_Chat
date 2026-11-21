package com.uzuu.diuchat.feature.journal.domain.repository

import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.MoodComparison
import com.uzuu.diuchat.feature.journal.domain.model.SearchResultItem
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getMonthlyMoodCheckins(
        month: Int,
        year: Int,
    ): Flow<List<DailyMoodCheckin>>

    suspend fun saveMoodCheckin(checkin: DailyMoodCheckin): Result<Unit>

    fun getMoodComparison(partnerId: String): Flow<MoodComparison>

    fun searchMessages(
        query: String,
        mood: JournalMoodType? = null,
        conversationId: String? = null,
    ): Flow<List<SearchResultItem>>
}
