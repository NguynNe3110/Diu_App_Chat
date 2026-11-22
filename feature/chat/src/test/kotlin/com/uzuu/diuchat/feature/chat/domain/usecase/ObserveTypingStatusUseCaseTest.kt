package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ObserveTypingStatusUseCaseTest {
    private val repository: ChatRepository = mockk()
    private val sut = ObserveTypingStatusUseCase(repository)

    @Test
    fun `invoke delegates to repository observeTypingStatus`() = runTest {
        every { repository.observeTypingStatus("conv_1") } returns flowOf(true)

        val result = sut("conv_1").first()

        result shouldBeEqualTo true
        verify(exactly = 1) { repository.observeTypingStatus("conv_1") }
    }
}
