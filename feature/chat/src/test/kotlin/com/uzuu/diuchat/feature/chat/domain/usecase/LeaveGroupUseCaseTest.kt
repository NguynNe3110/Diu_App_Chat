package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.GroupError
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

class LeaveGroupUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = LeaveGroupUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository leaveGroup`() = runTest {
        // given
        val convoId = "group_1"
        coEvery { chatRepository.leaveGroup(convoId) } just runs

        // when
        sut(convoId)

        // then
        coVerify(exactly = 1) { chatRepository.leaveGroup(convoId) }
    }

    @Test
    fun `invoke throws GroupError LastAdminCannotLeave when DEC-07 rule triggered`() = runTest {
        // given
        val convoId = "group_1"
        coEvery { chatRepository.leaveGroup(convoId) } throws GroupError.LastAdminCannotLeave

        // when / then
        assertThrows<GroupError.LastAdminCannotLeave> {
            sut(convoId)
        }
    }
}
