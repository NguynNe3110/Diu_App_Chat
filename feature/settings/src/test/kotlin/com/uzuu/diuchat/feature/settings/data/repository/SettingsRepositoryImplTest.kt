package com.uzuu.diuchat.feature.settings.data.repository

import com.uzuu.diuchat.feature.settings.data.datasource.api.SettingRemoteDataSource
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.response.UserSettingsResponse
import com.uzuu.diuchat.feature.settings.data.mapper.UserProfileMapper
import com.uzuu.diuchat.feature.settings.data.mapper.UserSettingsMapper
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

class SettingsRepositoryImplTest {
    private val remoteDataSource: SettingRemoteDataSource = mockk(relaxed = true)
    private val sut = SettingsRepositoryImpl(
        remoteDataSource = remoteDataSource,
        userProfileMapper = UserProfileMapper(),
        userSettingsMapper = UserSettingsMapper(),
    )

    @Test
    fun `themeMode updates locally and syncs to remote`() = runTest {
        val initial = sut.getThemeMode().first()
        initial shouldBeEqualTo "SYSTEM"

        sut.updateThemeMode("DARK")
        val updated = sut.getThemeMode().first()
        updated shouldBeEqualTo "DARK"

        coVerify {
            remoteDataSource.upsertUserSettings(
                match { it.userId == "user_me" && it.themeMode == "DARK" },
            )
        }
    }

    @Test
    fun `readReceiptsEnabled updates and reads correctly`() = runTest {
        val initial = sut.getReadReceiptsEnabled().first()
        initial shouldBeEqualTo true

        sut.updateReadReceiptsEnabled(false)
        val updated = sut.getReadReceiptsEnabled().first()
        updated shouldBeEqualTo false

        coVerify {
            remoteDataSource.upsertUserSettings(
                match { it.userId == "user_me" && !it.readReceiptsEnabled },
            )
        }
    }

    @Test
    fun `updateUserProfile maps domain to dto and upserts remotely`() = runTest {
        val updatedProfile = UserProfile(
            id = "user_me",
            displayName = "Linh",
            username = "linh_diu",
            bio = "Dịu dàng",
            birthday = "02/09/2001",
            phone = "+84901234567",
            primaryMood = "Hạnh phúc",
            avatarUrl = "https://example.com/linh.png",
        )

        sut.updateUserProfile(updatedProfile)

        sut.getUserProfile().first() shouldBeEqualTo updatedProfile
        coVerify {
            remoteDataSource.upsertUserProfile(
                UserProfileDto(
                    id = "user_me",
                    displayName = "Linh",
                    username = "linh_diu",
                    bio = "Dịu dàng",
                    birthday = "02/09/2001",
                    phone = "+84901234567",
                    primaryMood = "Hạnh phúc",
                    avatarUrl = "https://example.com/linh.png",
                ),
            )
        }
    }

    @Test
    fun `syncFromRemote updates local profile and settings from bundle`() = runTest {
        coEvery { remoteDataSource.fetchUserBundle() } returns UserSettingsResponse(
            profile = UserProfileDto(
                id = "remote-1",
                displayName = "An",
                username = "an_diu",
                bio = "Remote bio",
                birthday = "10/10/1999",
                phone = "+84988776655",
                primaryMood = "Bình yên",
                avatarUrl = null,
            ),
            settings = UserSettingsDto(
                userId = "remote-1",
                themeMode = "DARK",
                readReceiptsEnabled = false,
                softSendAnimationEnabled = false,
                notificationPrivacy = NotificationPrivacy.SENDER_NAME.name,
                notificationSound = NotificationSound.FALLING_LEAF.name,
                quietHoursEnabled = false,
                quietHoursStartMinute = 1380,
                quietHoursEndMinute = 390,
                quietHoursTimezone = "Asia/Ho_Chi_Minh",
                quietHoursAllowedContacts = listOf("friend-1"),
                quietHoursRepeatDays = listOf(1, 2, 3),
            ),
        )

        sut.syncFromRemote()

        sut.getUserProfile().first().id shouldBeEqualTo "remote-1"
        sut.getUserProfile().first().displayName shouldBeEqualTo "An"
        sut.getThemeMode().first() shouldBeEqualTo "DARK"
        sut.getReadReceiptsEnabled().first() shouldBeEqualTo false
        sut.getSoftSendAnimationEnabled().first() shouldBeEqualTo false
        sut.getNotificationPrivacy().first() shouldBeEqualTo NotificationPrivacy.SENDER_NAME
        sut.getNotificationSound().first() shouldBeEqualTo NotificationSound.FALLING_LEAF
        sut.getQuietSchedule().first() shouldBeEqualTo QuietSchedule(
            enabled = false,
            startMinuteOfDay = 1380,
            endMinuteOfDay = 390,
            timeZoneId = "Asia/Ho_Chi_Minh",
            allowedEmergencyContactIds = setOf("friend-1"),
            repeatDays = setOf(1, 2, 3),
        )
    }

    @Test
    fun `deleteAccount with matching phone number anonymizes user profile and signs out`() = runTest {
        val currentPhone = sut.getUserProfile().first().phone
        sut.deleteAccount(currentPhone)

        val updated = sut.getUserProfile().first()
        updated.displayName shouldBeEqualTo "Người dùng Dịu"
        updated.username shouldBeEqualTo "deleted_user"
        updated.phone shouldBeEqualTo ""

        coVerify {
            remoteDataSource.upsertUserProfile(match { it.username == "deleted_user" && it.phone == null })
            remoteDataSource.signOut()
        }
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
    fun `logout clears active user profile and signs out remotely`() = runTest {
        sut.logout()

        val updated = sut.getUserProfile().first()
        updated.id shouldBeEqualTo ""
        updated.displayName shouldBeEqualTo ""
        updated.username shouldBeEqualTo ""
        updated.phone shouldBeEqualTo ""

        coVerify { remoteDataSource.signOut() }
    }
}
