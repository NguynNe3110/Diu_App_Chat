package com.uzuu.diuchat.feature.letters.data.datasource.api.service

import com.uzuu.diuchat.feature.letters.data.datasource.api.model.LetterDto
import com.uzuu.diuchat.feature.letters.data.datasource.api.response.LettersResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException

interface LettersSupabaseService {
    suspend fun fetchLetters(): Result<LettersResponse>

    suspend fun upsertLetter(letter: LetterDto): Result<Unit>

    suspend fun deleteLetter(letterId: String): Result<Unit>
}

class LettersSupabaseServiceImpl(
    private val supabaseClient: SupabaseClient?,
) : LettersSupabaseService {
    override suspend fun fetchLetters(): Result<LettersResponse> {
        val client = supabaseClient ?: return Result.success(LettersResponse())
        return runCatching {
            val letters =
                client.postgrest
                    .from(TABLE_SEALED_LETTERS)
                    .select()
                    .decodeList<LetterDto>()
            LettersResponse(letters = letters)
        }.onFailure(::rethrowCancellation)
    }

    override suspend fun upsertLetter(letter: LetterDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_SEALED_LETTERS).upsert(letter)
        }.map { }.onFailure(::rethrowCancellation)
    }

    override suspend fun deleteLetter(letterId: String): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_SEALED_LETTERS).delete {
                filter {
                    eq("id", letterId)
                }
            }
        }.map { }.onFailure(::rethrowCancellation)
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private companion object {
        const val TABLE_SEALED_LETTERS = "sealed_letters"
    }
}
