package com.uzuu.diuchat.feature.home.domain.usecase

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetHomeStarsUseCaseTest {
    private val homeRepository: HomeRepository = mockk()
    private val sut = GetHomeStarsUseCase(homeRepository)

    @Test
    fun `invoke delegates to repository getStars`() = runTest {
        val expected = listOf(StarPerson("1", "Minh", "M", Color.Red, isQuiet = true))
        every { homeRepository.getStars(true) } returns flowOf(expected)

        val result = sut(isQuiet = true).first()

        assertEquals(expected, result)
    }
}
