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

class GetConversationUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = GetConversationUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository observeConversation`() = runTest {
        val convo = Conversation(
            id = "c1",
            title = "Minh",
            mood = MoodType.BINHYEN,
            lastMessage = "",
            lastMessageTimeEpochMs = 0L,
        )
        every { chatRepository.observeConversation("c1") } returns flowOf(convo)

        sut("c1").first() shouldBeEqualTo convo
    }
}
