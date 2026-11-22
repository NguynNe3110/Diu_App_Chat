package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetConversationsUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = GetConversationsUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository observeConversations`() = runTest {
        val convos = listOf(
            Conversation(
                id = "c1",
                title = "Minh",
                mood = MoodType.BINHYEN,
                lastMessage = "",
                lastMessageTimeEpochMs = 0L,
            ),
        )
        every { chatRepository.observeConversations() } returns flowOf(convos)

        sut().first() shouldBeEqualTo convos
    }
}
