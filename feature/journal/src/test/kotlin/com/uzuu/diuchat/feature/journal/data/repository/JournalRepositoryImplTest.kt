package com.uzuu.diuchat.feature.journal.data.repository

import com.uzuu.diuchat.feature.journal.data.datasource.api.service.JournalSupabaseService
import com.uzuu.diuchat.feature.journal.data.mapper.JournalMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class JournalRepositoryImplTest {
    private val mockSupabaseService: JournalSupabaseService = mockk {
        coEvery { fetchMonthlyCheckins(any(), any()) } returns Result.failure(IllegalStateException("offline"))
        coEvery { fetchFavourites() } returns Result.failure(IllegalStateException("offline"))
        coEvery { upsertMoodCheckin(any()) } returns Result.success(Unit)
        coEvery { insertFavourite(any()) } returns Result.success(Unit)
        coEvery { deleteFavourite(any()) } returns Result.success(Unit)
    }
    private val mapper = JournalMapper()
    private val sut = JournalRepositoryImpl(
        supabaseService = mockSupabaseService,
        mapper = mapper,
    )

    @Test
    fun `getMonthlyMoodCheckins filters by month and year`() = runTest {
        // Seeded checkins are for November 2026 (month 11, year 2026)
        val novemberCheckins = sut.getMonthlyMoodCheckins(month = 11, year = 2026).first()
        novemberCheckins.size shouldBeEqualTo 24
        novemberCheckins.all { it.dateIso.startsWith("2026-11") } shouldBeEqualTo true

        // For December 2026, no checkins should be returned
        val decemberCheckins = sut.getMonthlyMoodCheckins(month = 12, year = 2026).first()
        decemberCheckins.size shouldBeEqualTo 0

        // For November 2025, no checkins should be returned
        val pastYearCheckins = sut.getMonthlyMoodCheckins(month = 11, year = 2025).first()
        pastYearCheckins.size shouldBeEqualTo 0
    }
}
