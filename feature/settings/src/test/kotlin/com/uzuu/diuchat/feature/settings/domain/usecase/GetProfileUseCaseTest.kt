package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetProfileUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()

    val sut = GetProfileUseCase(settingsRepository)

    @Test
    fun `getProfile delegates to repository`() {
        runBlocking {
            val expected = UserProfile(
                id = "u1",
                displayName = "Minh Anh",
                username = "minhanh",
                bio = "Nhẹ nhàng lắng nghe",
                birthday = "15/08/2000",
                phone = "+84901234567",
                primaryMood = "Bình yên",
                avatarUrl = null,
            )
            every { settingsRepository.getUserProfile() } returns flowOf(expected)

            val actual = sut().first()

            actual shouldBeEqualTo expected
        }
    }
}
