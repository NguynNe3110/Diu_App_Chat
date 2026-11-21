package com.uzuu.diuchat.feature.journal.data.datasource.api.service

import com.uzuu.diuchat.feature.journal.data.datasource.api.model.FavouriteMemoryDto
import com.uzuu.diuchat.feature.journal.data.datasource.api.model.MoodCheckinDto
import com.uzuu.diuchat.feature.journal.data.datasource.api.response.JournalCheckinsResponse

interface JournalSupabaseService {
    suspend fun fetchMonthlyCheckins(
        month: Int,
        year: Int,
    ): Result<JournalCheckinsResponse>

    suspend fun upsertMoodCheckin(checkin: MoodCheckinDto): Result<Unit>

    suspend fun fetchFavourites(): Result<List<FavouriteMemoryDto>>

    suspend fun insertFavourite(memory: FavouriteMemoryDto): Result<Unit>

    suspend fun deleteFavourite(memoryId: String): Result<Unit>
}
