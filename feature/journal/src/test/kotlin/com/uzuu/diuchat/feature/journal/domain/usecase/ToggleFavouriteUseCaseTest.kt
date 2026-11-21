package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ToggleFavouriteUseCaseTest {

    private val journalRepository: JournalRepository = mockk()
    private val sut = ToggleFavouriteUseCase(journalRepository)

    @Test
    fun `invoke delegates to repository toggleFavourite`() = runTest {
        coEvery { journalRepository.toggleFavourite("fav_1") } returns Result.success(Unit)

        val result = sut("fav_1")

        result.isSuccess shouldBeEqualTo true
        coVerify(exactly = 1) { journalRepository.toggleFavourite("fav_1") }
    }
}
