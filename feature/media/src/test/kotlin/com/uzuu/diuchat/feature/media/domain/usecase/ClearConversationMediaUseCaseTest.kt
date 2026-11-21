package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClearConversationMediaUseCaseTest {

    private val mediaRepository: MediaRepository = mockk()
    private val sut = ClearConversationMediaUseCase(mediaRepository)

    @Test
    fun `invoke delegates to repository clearConversationMedia`() = runTest {
        coEvery { mediaRepository.clearConversationMedia("minh") } returns Result.success(500_000L)

        val result = sut("minh")

        assertTrue(result.isSuccess)
        assertEquals(500_000L, result.getOrNull())
    }
}
