package com.uzuu.diuchat.feature.journal.data.datasource.api.service

import com.uzuu.diuchat.feature.journal.data.datasource.api.model.FavouriteMemoryDto
import com.uzuu.diuchat.feature.journal.data.datasource.api.model.MoodCheckinDto
import com.uzuu.diuchat.feature.journal.data.datasource.api.response.JournalCheckinsResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException

class JournalSupabaseServiceImpl(
    private val supabaseClient: SupabaseClient?,
) : JournalSupabaseService {
    override suspend fun fetchMonthlyCheckins(
        month: Int,
        year: Int,
    ): Result<JournalCheckinsResponse> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            val datePrefix = "%04d-%02d".format(year, month)
            val checkins =
                client.postgrest
                    .from(MOOD_CHECKINS_TABLE)
                    .select {
                        filter {
                            like("date_iso", "$datePrefix%")
                        }
                    }.decodeList<MoodCheckinDto>()
            JournalCheckinsResponse(checkins = checkins)
        }.onFailure(::rethrowCancellation)
    }

    override suspend fun upsertMoodCheckin(checkin: MoodCheckinDto): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.postgrest
                .from(MOOD_CHECKINS_TABLE)
                .upsert(checkin)
        }.map {}.onFailure(::rethrowCancellation)
    }

    override suspend fun fetchFavourites(): Result<List<FavouriteMemoryDto>> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.postgrest
                .from(FAVOURITE_MEMORIES_TABLE)
                .select()
                .decodeList<FavouriteMemoryDto>()
        }.onFailure(::rethrowCancellation)
    }

    override suspend fun insertFavourite(memory: FavouriteMemoryDto): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.postgrest
                .from(FAVOURITE_MEMORIES_TABLE)
                .upsert(memory)
        }.map {}.onFailure(::rethrowCancellation)
    }

    override suspend fun deleteFavourite(memoryId: String): Result<Unit> {
        val client = supabaseClient ?: return missingClientFailure()
        return runCatching {
            client.postgrest
                .from(FAVOURITE_MEMORIES_TABLE)
                .delete {
                    filter {
                        eq("id", memoryId)
                    }
                }
        }.map {}.onFailure(::rethrowCancellation)
    }

    private fun <T> missingClientFailure(): Result<T> = Result.failure(IllegalStateException("SupabaseClient chưa được khởi tạo."))

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private companion object {
        private const val MOOD_CHECKINS_TABLE = "mood_checkins"
        private const val FAVOURITE_MEMORIES_TABLE = "favourite_memories"
    }
}
