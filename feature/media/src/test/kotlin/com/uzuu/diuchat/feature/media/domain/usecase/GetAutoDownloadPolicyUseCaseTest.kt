package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetAutoDownloadPolicyUseCaseTest {

    private val mediaRepository: MediaRepository = mockk()
    private val sut = GetAutoDownloadPolicyUseCase(mediaRepository)

    @Test
    fun `invoke delegates to repository getAutoDownloadPolicy`() = runTest {
        every { mediaRepository.getAutoDownloadPolicy() } returns flowOf(AutoDownloadPolicy.CELLULAR_AND_WIFI)

        val result = sut().first()

        assertEquals(AutoDownloadPolicy.CELLULAR_AND_WIFI, result)
    }
}
