package com.uzuu.diuchat.feature.home.domain.usecase

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetRecentChatsUseCaseTest {
    private val homeRepository: HomeRepository = mockk()
    private val sut = GetRecentChatsUseCase(homeRepository)

    @Test
    fun `invoke delegates to repository getRecentChats`() = runTest {
        val expected = listOf(RecentChat("1", "Minh", "M", Color.Red, "Hi", "10:00"))
        every { homeRepository.getRecentChats() } returns flowOf(expected)

        val result = sut().first()

        assertEquals(expected, result)
    }
}
