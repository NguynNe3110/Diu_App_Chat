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

class UpdateGroupInfoUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = UpdateGroupInfoUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository updateGroupInfo`() = runTest {
        // given
        val convoId = "group_1"
        val title = "Nhà Dịu"
        val mood = MoodType.BINHYEN
        val nickname = "Bé ngoan"
        val isMuted = true

        coEvery {
            chatRepository.updateGroupInfo(convoId, title, mood, nickname, isMuted)
        } just runs

        // when
        sut(convoId, title, mood, nickname, isMuted)

        // then
        coVerify(exactly = 1) {
            chatRepository.updateGroupInfo(convoId, title, mood, nickname, isMuted)
        }
    }
}
