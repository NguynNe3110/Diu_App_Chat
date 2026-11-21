package com.uzuu.diuchat.feature.settings.domain.usecase

import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetBlockedContactsUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()

    val sut = GetBlockedContactsUseCase(settingsRepository)

    @Test
    fun `getBlockedContacts delegates to repository`() {
        runBlocking {
            val expected = listOf(
                BlockedContact("1", "User A", "+84900000001", "10/11/2026"),
            )
            every { settingsRepository.getBlockedContacts() } returns flowOf(expected)

            val actual = sut().first()

            actual shouldBeEqualTo expected
        }
    }
}
