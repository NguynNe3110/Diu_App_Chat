package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class UpdateProfileUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()

    val sut = UpdateProfileUseCase(settingsRepository)

    @Test
    fun `updateProfile delegates to repository`() {
        runBlocking {
            val profile = UserProfile(
                id = "u1",
                displayName = "Minh Anh Mới",
                username = "minhanh",
                bio = "Dịu dàng với cuộc đời",
                birthday = "15/08/2000",
                phone = "+84901234567",
                primaryMood = "Dịu êm",
                avatarUrl = null,
            )
            coEvery { settingsRepository.updateUserProfile(profile) } returns Unit

            sut(profile)

            coVerify { settingsRepository.updateUserProfile(profile) }
        }
    }
}
