package com.uzuu.diuchat.feature.media.domain.usecase

import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateAutoDownloadPolicyUseCaseTest {

    private val mediaRepository: MediaRepository = mockk()
    private val sut = UpdateAutoDownloadPolicyUseCase(mediaRepository)

    @Test
    fun `invoke delegates to repository updateAutoDownloadPolicy`() = runTest {
        coEvery { mediaRepository.updateAutoDownloadPolicy(AutoDownloadPolicy.MANUAL) } just runs

        sut(AutoDownloadPolicy.MANUAL)

        coVerify(exactly = 1) { mediaRepository.updateAutoDownloadPolicy(AutoDownloadPolicy.MANUAL) }
    }
}
