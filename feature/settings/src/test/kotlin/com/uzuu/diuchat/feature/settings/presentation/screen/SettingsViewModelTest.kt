package com.uzuu.diuchat.feature.settings.presentation.screen

import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.usecase.GetBlockedContactsUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UnblockContactUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateQuietScheduleUseCase
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

    private val getBlockedContactsUseCase: GetBlockedContactsUseCase = mockk()
    private val getNotificationPrivacyUseCase: GetNotificationPrivacyUseCase = mockk()
    private val getQuietScheduleUseCase: GetQuietScheduleUseCase = mockk()
    private val unblockContactUseCase: UnblockContactUseCase = mockk(relaxed = true)
    private val updateNotificationPrivacyUseCase: UpdateNotificationPrivacyUseCase = mockk(relaxed = true)
    private val updateQuietScheduleUseCase: UpdateQuietScheduleUseCase = mockk(relaxed = true)

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
        every { getQuietScheduleUseCase() } returns flowOf(testQuietSchedule)
        every { getNotificationPrivacyUseCase() } returns flowOf(testNotificationPrivacy)
        every { getBlockedContactsUseCase() } returns flowOf(testBlockedContacts)

        sut = SettingsViewModel(
            getBlockedContactsUseCase = getBlockedContactsUseCase,
            getNotificationPrivacyUseCase = getNotificationPrivacyUseCase,
            getQuietScheduleUseCase = getQuietScheduleUseCase,
            unblockContactUseCase = unblockContactUseCase,
            updateNotificationPrivacyUseCase = updateNotificationPrivacyUseCase,
            updateQuietScheduleUseCase = updateQuietScheduleUseCase,
        )
    }

    @Test
    fun `initial state collects repository flows`() = runTest {
        advanceUntilIdle()

        sut.uiStateFlow.value.quietSchedule shouldBeEqualTo testQuietSchedule
        sut.uiStateFlow.value.notificationPrivacy shouldBeEqualTo testNotificationPrivacy
        sut.uiStateFlow.value.blockedContacts shouldBeEqualTo testBlockedContacts
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
        sut.uiStateFlow.value.subScreen shouldBeEqualTo SettingsSubScreen.OVERVIEW
    }

    @Test
    fun `toggleReadReceipts updates state`() = runTest {
        sut.toggleReadReceipts(false)
        sut.uiStateFlow.value.readReceiptsEnabled shouldBeEqualTo false
    }
}
