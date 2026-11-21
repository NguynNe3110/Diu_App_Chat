package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClearCacheUseCaseTest {

    private val mockMediaRepository: MediaRepository = mockk()
    private val sut = ClearCacheUseCase(mockMediaRepository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        coEvery { mockMediaRepository.clearCache() } returns Result.success(200L)

        val result = sut()

        assertTrue(result.isSuccess)
        assertEquals(200L, result.getOrNull())
    }
}
