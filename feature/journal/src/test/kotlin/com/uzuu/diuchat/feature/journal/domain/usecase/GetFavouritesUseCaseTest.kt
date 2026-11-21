package com.uzuu.diuchat.feature.journal.domain.usecase

import com.uzuu.diuchat.feature.journal.domain.model.FavouriteMemory
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetFavouritesUseCaseTest {

    private val journalRepository: JournalRepository = mockk()
    private val sut = GetFavouritesUseCase(journalRepository)

    @Test
    fun `invoke delegates to repository getFavourites`() = runTest {
        val expected = listOf(
            FavouriteMemory(
                id = "fav_1",
                senderName = "Minh",
                mood = JournalMoodType.THUONG,
                content = "Cảm ơn vì đã luôn ở bên mình.",
                savedDate = "14/02/2024",
            ),
        )
        every { journalRepository.getFavourites() } returns flowOf(expected)

        val actual = sut().first()

        actual shouldBeEqualTo expected
        verify(exactly = 1) { journalRepository.getFavourites() }
    }
}
