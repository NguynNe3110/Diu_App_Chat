package com.uzuu.diuchat.feature.home.domain.usecase

import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteHomeConversationUseCaseTest {
    private val homeRepository: HomeRepository = mockk()
    private val sut = DeleteHomeConversationUseCase(homeRepository)

    @Test
    fun `invoke delegates to homeRepository deleteConversation`() = runTest {
        coEvery { homeRepository.deleteConversation("conv_1") } just runs

        sut("conv_1")

        coVerify(exactly = 1) { homeRepository.deleteConversation("conv_1") }
    }
}
