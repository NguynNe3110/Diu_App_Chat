package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class CreateGroupUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = CreateGroupUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository createGroup`() = runTest {
        // given
        val title = "Gia đình nhỏ"
        val mood = MoodType.THUONG
        val memberIds = listOf("user_linh", "user_nam")
        val expected = Conversation(
            id = "group_123",
            title = title,
            mood = mood,
            lastMessage = "Nhóm vừa được tạo",
            lastMessageTimeEpochMs = 1000L,
            isGroup = true,
        )

        coEvery {
            chatRepository.createGroup(title, mood, memberIds)
        } returns expected

        // when
        val actual = sut(title, mood, memberIds)

        // then
        actual shouldBeEqualTo expected
        coVerify(exactly = 1) {
            chatRepository.createGroup(title, mood, memberIds)
        }
    }
}
