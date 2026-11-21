package com.uzuu.diuchat.feature.settings.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

class SettingsRepositoryImplTest {
    private val sut = SettingsRepositoryImpl()

    @Test
    fun `themeMode updates and reads correctly`() = runTest {
        val initial = sut.getThemeMode().first()
        initial shouldBeEqualTo "SYSTEM"

        sut.updateThemeMode("DARK")
        val updated = sut.getThemeMode().first()
        updated shouldBeEqualTo "DARK"
    }

    @Test
    fun `readReceiptsEnabled updates and reads correctly`() = runTest {
        val initial = sut.getReadReceiptsEnabled().first()
        initial shouldBeEqualTo true

        sut.updateReadReceiptsEnabled(false)
        val updated = sut.getReadReceiptsEnabled().first()
        updated shouldBeEqualTo false
    }

    @Test
    fun `deleteAccount with matching phone number anonymizes user profile`() = runTest {
        val currentPhone = sut.getUserProfile().first().phone
        sut.deleteAccount(currentPhone)

        val updated = sut.getUserProfile().first()
        updated.displayName shouldBeEqualTo "Người dùng Dịu"
        updated.username shouldBeEqualTo "deleted_user"
        updated.phone shouldBeEqualTo ""
    }

    @Test
    fun `deleteAccount with non-matching phone number throws IllegalArgumentException`() = runTest {
        val action = {
            kotlinx.coroutines.runBlocking {
                sut.deleteAccount("+84999999999")
            }
        }
        action shouldThrow IllegalArgumentException::class
    }

    @Test
    fun `logout clears active user profile`() = runTest {
        sut.logout()

        val updated = sut.getUserProfile().first()
        updated.id shouldBeEqualTo ""
        updated.displayName shouldBeEqualTo ""
        updated.username shouldBeEqualTo ""
        updated.phone shouldBeEqualTo ""
    }
}
