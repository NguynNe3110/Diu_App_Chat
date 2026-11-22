package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class StarMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = StarMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates to chatRepository starMessage`() = runTest {
        coEvery { chatRepository.starMessage("msg_1") } returns true

        val result = sut("msg_1")

        result shouldBeEqualTo true
        coVerify(exactly = 1) { chatRepository.starMessage("msg_1") }
    }
}
