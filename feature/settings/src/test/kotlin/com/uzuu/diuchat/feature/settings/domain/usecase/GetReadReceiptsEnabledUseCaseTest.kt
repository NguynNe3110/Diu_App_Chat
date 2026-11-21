package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetReadReceiptsEnabledUseCaseTest {
    private val settingsRepository: SettingsRepository = mockk()

    val sut = GetReadReceiptsEnabledUseCase(settingsRepository)

    @Test
    fun `delegates to repository getReadReceiptsEnabled`() = runTest {
        every { settingsRepository.getReadReceiptsEnabled() } returns flowOf(true)

        val actual = sut().first()

        actual shouldBeEqualTo true
    }
}
