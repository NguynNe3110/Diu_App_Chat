package com.uzuu.diuchat.feature.settings.presentation.screen

import com.uzuu.diuchat.feature.settings.domain.model.AccountDeletionState
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.domain.usecase.DeleteAccountUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetBlockedContactsUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationSoundUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetReadReceiptsEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetSoftSendAnimationEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetThemeModeUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.LogoutUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UnblockContactUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationSoundUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateReadReceiptsEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateSoftSendAnimationEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateThemeModeUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestDispatcherExtension = CoroutinesTestDispatcherExtension()

    private val deleteAccountUseCase: DeleteAccountUseCase = mockk(relaxed = true)
    private val getBlockedContactsUseCase: GetBlockedContactsUseCase = mockk()
    private val getNotificationPrivacyUseCase: GetNotificationPrivacyUseCase = mockk()
    private val getNotificationSoundUseCase: GetNotificationSoundUseCase = mockk()
    private val getProfileUseCase: GetProfileUseCase = mockk()
    private val getQuietScheduleUseCase: GetQuietScheduleUseCase = mockk()
    private val getReadReceiptsEnabledUseCase: GetReadReceiptsEnabledUseCase = mockk()
    private val getSoftSendAnimationEnabledUseCase: GetSoftSendAnimationEnabledUseCase = mockk()
    private val getThemeModeUseCase: GetThemeModeUseCase = mockk()
    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)
    private val unblockContactUseCase: UnblockContactUseCase = mockk(relaxed = true)
    private val updateNotificationPrivacyUseCase: UpdateNotificationPrivacyUseCase = mockk(relaxed = true)
    private val updateNotificationSoundUseCase: UpdateNotificationSoundUseCase = mockk(relaxed = true)
    private val updateProfileUseCase: UpdateProfileUseCase = mockk(relaxed = true)
    private val updateQuietScheduleUseCase: UpdateQuietScheduleUseCase = mockk(relaxed = true)
    private val updateReadReceiptsEnabledUseCase: UpdateReadReceiptsEnabledUseCase = mockk(relaxed = true)
    private val updateSoftSendAnimationEnabledUseCase: UpdateSoftSendAnimationEnabledUseCase = mockk(relaxed = true)
    private val updateThemeModeUseCase: UpdateThemeModeUseCase = mockk(relaxed = true)

    private val testProfile = UserProfile(
        id = "u1",
        displayName = "Minh Anh",
        username = "minhanh",
        bio = "Lắng nghe nhẹ nhàng",
        birthday = "15/08/2000",
        phone = "+84901234567",
        primaryMood = "Bình yên",
        avatarUrl = null,
    )
    private val testQuietSchedule = QuietSchedule(
        enabled = true,
        startMinuteOfDay = 1350,
        endMinuteOfDay = 360,
    )
    private val testNotificationPrivacy = NotificationPrivacy.FULL_CONTENT
    private val testBlockedContacts = listOf(
        BlockedContact("b1", "Spam", "09001", "12/11/2026"),
    )

    lateinit var sut: SettingsViewModel

    @BeforeEach
    fun setUp() {
        every { getProfileUseCase() } returns flowOf(testProfile)
        every { getQuietScheduleUseCase() } returns flowOf(testQuietSchedule)
        every { getNotificationPrivacyUseCase() } returns flowOf(testNotificationPrivacy)
        every { getNotificationSoundUseCase() } returns flowOf(NotificationSound.WATER_DROP)
        every { getBlockedContactsUseCase() } returns flowOf(testBlockedContacts)
        every { getReadReceiptsEnabledUseCase() } returns flowOf(true)
        every { getSoftSendAnimationEnabledUseCase() } returns flowOf(true)
        every { getThemeModeUseCase() } returns flowOf("SYSTEM")

        sut = SettingsViewModel(
            deleteAccountUseCase = deleteAccountUseCase,
            getBlockedContactsUseCase = getBlockedContactsUseCase,
            getNotificationPrivacyUseCase = getNotificationPrivacyUseCase,
            getNotificationSoundUseCase = getNotificationSoundUseCase,
            getProfileUseCase = getProfileUseCase,
            getQuietScheduleUseCase = getQuietScheduleUseCase,
            getReadReceiptsEnabledUseCase = getReadReceiptsEnabledUseCase,
            getSoftSendAnimationEnabledUseCase = getSoftSendAnimationEnabledUseCase,
            getThemeModeUseCase = getThemeModeUseCase,
            logoutUseCase = logoutUseCase,
            unblockContactUseCase = unblockContactUseCase,
            updateNotificationPrivacyUseCase = updateNotificationPrivacyUseCase,
            updateNotificationSoundUseCase = updateNotificationSoundUseCase,
            updateProfileUseCase = updateProfileUseCase,
            updateQuietScheduleUseCase = updateQuietScheduleUseCase,
            updateReadReceiptsEnabledUseCase = updateReadReceiptsEnabledUseCase,
            updateSoftSendAnimationEnabledUseCase = updateSoftSendAnimationEnabledUseCase,
            updateThemeModeUseCase = updateThemeModeUseCase,
        )
    }

    @Test
    fun `initial state collects repository flows`() = runTest {
        advanceUntilIdle()

        sut.uiStateFlow.value.userProfile shouldBeEqualTo testProfile
        sut.uiStateFlow.value.quietSchedule shouldBeEqualTo testQuietSchedule
        sut.uiStateFlow.value.notificationPrivacy shouldBeEqualTo testNotificationPrivacy
        sut.uiStateFlow.value.notificationSound shouldBeEqualTo NotificationSound.WATER_DROP
        sut.uiStateFlow.value.blockedContacts shouldBeEqualTo testBlockedContacts
        sut.uiStateFlow.value.readReceiptsEnabled shouldBeEqualTo true
        sut.uiStateFlow.value.softSendAnimationEnabled shouldBeEqualTo true
        sut.uiStateFlow.value.themeMode shouldBeEqualTo "SYSTEM"
    }

    @Test
    fun `toggleSoftSendAnimation invokes usecase`() = runTest {
        sut.toggleSoftSendAnimation(false)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateSoftSendAnimationEnabledUseCase(false) }
    }

    @Test
    fun `setNotificationSound invokes usecase`() = runTest {
        sut.setNotificationSound(NotificationSound.WIND_CHIME)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateNotificationSoundUseCase(NotificationSound.WIND_CHIME) }
    }

    @Test
    fun `toggleQuietHours updates quiet schedule through usecase`() = runTest {
        advanceUntilIdle()

        sut.toggleQuietHours(false)
        advanceUntilIdle()

        coVerify {
            updateQuietScheduleUseCase(testQuietSchedule.copy(enabled = false))
        }
    }

    @Test
    fun `updateQuietTimeRange updates start and end minutes`() = runTest {
        advanceUntilIdle()

        sut.updateQuietTimeRange(1380, 420)
        advanceUntilIdle()

        coVerify {
            updateQuietScheduleUseCase(
                testQuietSchedule.copy(startMinuteOfDay = 1380, endMinuteOfDay = 420),
            )
        }
    }

    @Test
    fun `updatePrimaryMoodQuickly and stepQuietStartTime persist changes via usecases`() = runTest {
        advanceUntilIdle()
        sut.updatePrimaryMoodQuickly("Thương")
        advanceUntilIdle()
        coVerify { updateProfileUseCase(testProfile.copy(primaryMood = "Thương")) }

        sut.stepQuietStartTime(-30)
        advanceUntilIdle()
        coVerify { updateQuietScheduleUseCase(testQuietSchedule.copy(startMinuteOfDay = 1320)) }

        sut.stepQuietEndTime(30)
        advanceUntilIdle()
        coVerify { updateQuietScheduleUseCase(testQuietSchedule.copy(endMinuteOfDay = 390)) }
    }

    @Test
    fun `setNotificationPrivacy updates privacy and dismisses dialog`() = runTest {
        advanceUntilIdle()

        sut.setNotificationPrivacy(NotificationPrivacy.MOOD_COLOR_ONLY)
        advanceUntilIdle()

        coVerify {
            updateNotificationPrivacyUseCase(NotificationPrivacy.MOOD_COLOR_ONLY)
        }
        sut.uiStateFlow.value.showPrivacyDialog shouldBeEqualTo false
    }

    @Test
    fun `unblockContact invokes unblock usecase`() = runTest {
        advanceUntilIdle()

        sut.unblockContact("b1")
        advanceUntilIdle()

        coVerify {
            unblockContactUseCase("b1")
        }
    }

    @Test
    fun `navigation between overview and blocked contacts sub screens`() = runTest {
        sut.navigateToBlockedContacts()
        sut.uiStateFlow.value.subScreen shouldBeEqualTo SettingsSubScreen.BLOCKED_CONTACTS

        sut.navigateBackToOverview()
        sut.uiStateFlow.value.subScreen shouldBeEqualTo SettingsSubScreen.PROFILE_OVERVIEW
    }

    @Test
    fun `toggleReadReceipts invokes usecase`() = runTest {
        sut.toggleReadReceipts(false)
        advanceUntilIdle()

        coVerify {
            updateReadReceiptsEnabledUseCase(false)
        }
    }

    @Test
    fun `setThemeMode invokes usecase`() = runTest {
        sut.setThemeMode("DARK")
        advanceUntilIdle()

        coVerify {
            updateThemeModeUseCase("DARK")
        }
    }

    @Test
    fun `showEditProfile updates sheet visibility`() = runTest {
        sut.showEditProfile(true)
        sut.uiStateFlow.value.showEditProfileSheet shouldBeEqualTo true

        sut.showEditProfile(false)
        sut.uiStateFlow.value.showEditProfileSheet shouldBeEqualTo false
    }

    @Test
    fun `updateProfile invokes usecase and closes sheet`() = runTest {
        advanceUntilIdle()

        sut.updateProfile(
            displayName = "Minh Anh Mới",
            bio = "Dịu dàng với ngày qua",
            birthday = "15/08/2000",
            primaryMood = "Dịu êm",
        )
        advanceUntilIdle()

        coVerify {
            updateProfileUseCase(
                testProfile.copy(
                    displayName = "Minh Anh Mới",
                    bio = "Dịu dàng với ngày qua",
                    birthday = "15/08/2000",
                    primaryMood = "Dịu êm",
                ),
            )
        }
        sut.uiStateFlow.value.showEditProfileSheet shouldBeEqualTo false
    }

    @Test
    fun `showLogoutConfirm updates dialog visibility`() = runTest {
        sut.showLogoutConfirm(true)
        sut.uiStateFlow.value.showLogoutConfirmDialog shouldBeEqualTo true

        sut.showLogoutConfirm(false)
        sut.uiStateFlow.value.showLogoutConfirmDialog shouldBeEqualTo false
    }

    @Test
    fun `logout invokes logoutUseCase, dismisses dialog and sets isLoggedOut`() = runTest {
        sut.logout()
        advanceUntilIdle()

        coVerify { logoutUseCase() }
        sut.uiStateFlow.value.showLogoutConfirmDialog shouldBeEqualTo false
        sut.uiStateFlow.value.isLoggedOut shouldBeEqualTo true
    }

    @Test
    fun `showDeleteAccount updates dialog visibility`() = runTest {
        sut.showDeleteAccount(true)
        sut.uiStateFlow.value.showDeleteAccountDialog shouldBeEqualTo true

        sut.showDeleteAccount(false)
        sut.uiStateFlow.value.showDeleteAccountDialog shouldBeEqualTo false
    }

    @Test
    fun `deleteAccount success marks completed, closes dialog and sets isLoggedOut`() = runTest {
        advanceUntilIdle()

        sut.deleteAccount("+84901234567")
        advanceUntilIdle()

        coVerify { deleteAccountUseCase("+84901234567") }
        sut.uiStateFlow.value.accountDeletionState shouldBeEqualTo AccountDeletionState.Completed
        sut.uiStateFlow.value.showDeleteAccountDialog shouldBeEqualTo false
        sut.uiStateFlow.value.isLoggedOut shouldBeEqualTo true
    }

    @Test
    fun `deleteAccount failure sets deletion state to Failed`() = runTest {
        advanceUntilIdle()
        coEvery { deleteAccountUseCase(any()) } throws RuntimeException("Số điện thoại không khớp")

        sut.deleteAccount("0900000000")
        advanceUntilIdle()

        sut.uiStateFlow.value.accountDeletionState shouldBeEqualTo AccountDeletionState.Failed("Số điện thoại không khớp")
        sut.uiStateFlow.value.isLoggedOut shouldBeEqualTo false
    }

    @Test
    fun `updateProfile persists selected avatarUrl and closes edit sheet`() = runTest {
        advanceUntilIdle()
        val avatarUri = "content://media/picker/0/com.android.providers.media.photopicker/media/42"

        sut.updateProfile(
            displayName = "Minh Anh",
            bio = "Lắng nghe nhẹ nhàng",
            birthday = "15/08/2000",
            primaryMood = "Bình yên",
            avatarUrl = avatarUri,
        )
        advanceUntilIdle()

        coVerify {
            updateProfileUseCase(
                testProfile.copy(avatarUrl = avatarUri),
            )
        }
        sut.uiStateFlow.value.showEditProfileSheet shouldBeEqualTo false
    }
}
