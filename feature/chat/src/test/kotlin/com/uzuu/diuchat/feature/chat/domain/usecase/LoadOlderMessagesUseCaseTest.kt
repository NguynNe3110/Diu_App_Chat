package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class LoadOlderMessagesUseCaseTest {
    private val mockChatRepository: ChatRepository = mockk()
    private val sut = LoadOlderMessagesUseCase(mockChatRepository)

    @Test
    fun `invoke delegates to chatRepository loadOlderMessages and returns hasMore`() = runTest {
        coEvery {
            mockChatRepository.loadOlderMessages(
                conversationId = "conv_1",
                beforeEpochMs = 1_700_000_000_000L,
                limit = 20,
            )
        } returns true

        val hasMore = sut("conv_1", 1_700_000_000_000L, 20)

        hasMore shouldBeEqualTo true
        coVerify(exactly = 1) {
            mockChatRepository.loadOlderMessages("conv_1", 1_700_000_000_000L, 20)
        }
    }
}
