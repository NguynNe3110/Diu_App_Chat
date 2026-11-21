package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ToggleReactionUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = ToggleReactionUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository toggleReaction`() = runTest {
        // given
        val messageId = "msg_123"
        val mood = MoodType.THUONG
        coEvery { chatRepository.toggleReaction(messageId, mood) } just runs

        // when
        sut(messageId, mood)

        // then
        coVerify(exactly = 1) { chatRepository.toggleReaction(messageId, mood) }
    }
}
