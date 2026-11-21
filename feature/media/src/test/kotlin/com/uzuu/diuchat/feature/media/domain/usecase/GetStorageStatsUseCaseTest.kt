package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.model.StorageStats
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetStorageStatsUseCaseTest {

    private val mockMediaRepository: MediaRepository = mockk()
    private val sut = GetStorageStatsUseCase(mockMediaRepository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        val expected = StorageStats(
            totalStorageBytes = 1000L,
            photosBytes = 500L,
            voiceBytes = 300L,
            cacheBytes = 200L,
        )
        every { mockMediaRepository.getStorageStats() } returns flowOf(expected)

        val result = sut().first()

        assertEquals(expected, result)
    }
}
