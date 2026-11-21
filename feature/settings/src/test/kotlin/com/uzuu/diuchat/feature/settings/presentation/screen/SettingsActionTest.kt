package com.uzuu.diuchat.feature.settings.presentation.screen

import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SettingsActionTest {

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
}
