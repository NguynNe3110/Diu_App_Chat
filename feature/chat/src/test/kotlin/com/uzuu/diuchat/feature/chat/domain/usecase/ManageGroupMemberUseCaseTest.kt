package com.uzuu.diuchat.feature.chat.domain.usecase

import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ManageGroupMemberUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val sut = ManageGroupMemberUseCase(chatRepository)

    @Test
    fun `invoke delegates to repository manageGroupMember`() = runTest {
        // given
        val convoId = "group_1"
        val targetUserId = "user_linh"
        val newRole = GroupRole.ADMIN

        coEvery {
            chatRepository.manageGroupMember(convoId, targetUserId, newRole, false)
        } just runs

        // when
        sut(convoId, targetUserId, newRole, false)

        // then
        coVerify(exactly = 1) {
            chatRepository.manageGroupMember(convoId, targetUserId, newRole, false)
        }
    }
}
