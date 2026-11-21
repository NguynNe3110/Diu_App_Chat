package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetVaultMediaUseCaseTest {

    private val mockMediaRepository: MediaRepository = mockk()
    private val sut = GetVaultMediaUseCase(mockMediaRepository)

    @Test
    fun `invoke delegates to repository with conversationId`() = runTest {
        val expected = listOf(
            MediaVaultItem(
                id = "p1",
                conversationId = "minh",
                type = MediaType.PHOTO,
                caption = "Test Photo",
            ),
        )
        every { mockMediaRepository.getVaultMedia("minh") } returns flowOf(expected)

        val result = sut("minh").first()

        assertEquals(expected, result)
    }
}
