package com.uzuu.diuchat.feature.settings.presentation.screen

import com.uzuu.diuchat.feature.settings.domain.model.AccountDeletionState
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SettingsActionTest {

    @Test
    fun `SetUserProfile reduces userProfile in state`() {
        val initial = SettingsUiState()
        val profile = UserProfile(
            id = "u2",
            displayName = "Thanh Tâm",
            username = "thanhtam",
            bio = "Dịu dàng",
            birthday = "01/01/2001",
            phone = "+84909999999",
            primaryMood = "Ấm áp",
            avatarUrl = null,
        )
        val action = SettingsAction.SetUserProfile(profile)

        val newState = action.reduce(initial)

        newState.userProfile shouldBeEqualTo profile
    }

    @Test
    fun `SetQuietSchedule reduces quietSchedule in state`() {
        val initial = SettingsUiState()
        val schedule = QuietSchedule(enabled = false, startMinuteOfDay = 1200, endMinuteOfDay = 400)
        val action = SettingsAction.SetQuietSchedule(schedule)

        val newState = action.reduce(initial)

        newState.quietSchedule shouldBeEqualTo schedule
    }

    @Test
    fun `SetNotificationPrivacy reduces notificationPrivacy in state`() {
        val initial = SettingsUiState()
        val action = SettingsAction.SetNotificationPrivacy(NotificationPrivacy.MOOD_COLOR_ONLY)

        val newState = action.reduce(initial)

        newState.notificationPrivacy shouldBeEqualTo NotificationPrivacy.MOOD_COLOR_ONLY
    }

    @Test
    fun `SetBlockedContacts reduces blockedContacts in state`() {
        val initial = SettingsUiState()
        val contacts = listOf(BlockedContact("id_1", "Spam", "09000", "today"))
        val action = SettingsAction.SetBlockedContacts(contacts)

        val newState = action.reduce(initial)

        newState.blockedContacts shouldBeEqualTo contacts
    }

    @Test
    fun `ToggleReadReceipts reduces readReceiptsEnabled in state`() {
        val initial = SettingsUiState(readReceiptsEnabled = true)
        val action = SettingsAction.ToggleReadReceipts(false)

        val newState = action.reduce(initial)

        newState.readReceiptsEnabled shouldBeEqualTo false
    }

    @Test
    fun `SetThemeMode reduces themeMode in state`() {
        val initial = SettingsUiState(themeMode = "SYSTEM")
        val action = SettingsAction.SetThemeMode("DARK")

        val newState = action.reduce(initial)

        newState.themeMode shouldBeEqualTo "DARK"
    }

    @Test
    fun `SetSubScreen reduces subScreen in state`() {
        val initial = SettingsUiState(subScreen = SettingsSubScreen.OVERVIEW)
        val action = SettingsAction.SetSubScreen(SettingsSubScreen.BLOCKED_CONTACTS)

        val newState = action.reduce(initial)

        newState.subScreen shouldBeEqualTo SettingsSubScreen.BLOCKED_CONTACTS
    }

    @Test
    fun `SetShowPrivacyDialog and SetShowQuietHoursDialog reduce dialog visibility`() {
        val initial = SettingsUiState()
        val s1 = SettingsAction.SetShowPrivacyDialog(true).reduce(initial)
        s1.showPrivacyDialog shouldBeEqualTo true

        val s2 = SettingsAction.SetShowQuietHoursDialog(true).reduce(initial)
        s2.showQuietHoursDialog shouldBeEqualTo true
    }

    @Test
    fun `SetShowEditProfileSheet reduces sheet visibility`() {
        val initial = SettingsUiState()
        val newState = SettingsAction.SetShowEditProfileSheet(true).reduce(initial)
        newState.showEditProfileSheet shouldBeEqualTo true
    }

    @Test
    fun `SetShowDeleteAccountDialog and SetShowLogoutConfirmDialog reduce dialog visibility`() {
        val initial = SettingsUiState()
        val s1 = SettingsAction.SetShowDeleteAccountDialog(true).reduce(initial)
        s1.showDeleteAccountDialog shouldBeEqualTo true

        val s2 = SettingsAction.SetShowLogoutConfirmDialog(true).reduce(initial)
        s2.showLogoutConfirmDialog shouldBeEqualTo true
    }

    @Test
    fun `SetAccountDeletionState reduces deletion state`() {
        val initial = SettingsUiState()
        val s1 = SettingsAction.SetAccountDeletionState(AccountDeletionState.Processing).reduce(initial)
        s1.accountDeletionState shouldBeEqualTo AccountDeletionState.Processing

        val s2 = SettingsAction.SetAccountDeletionState(AccountDeletionState.Completed).reduce(s1)
        s2.accountDeletionState shouldBeEqualTo AccountDeletionState.Completed
    }

    @Test
    fun `SetLoggedOut reduces isLoggedOut in state`() {
        val initial = SettingsUiState()
        val newState = SettingsAction.SetLoggedOut(true).reduce(initial)
        newState.isLoggedOut shouldBeEqualTo true
    }
}
