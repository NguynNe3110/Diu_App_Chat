package com.uzuu.diuchat.feature.settings.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule

sealed class SettingsAction : BaseAction<SettingsUiState> {
    data class SetQuietSchedule(val quietSchedule: QuietSchedule) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState =
            state.copy(quietSchedule = quietSchedule)
    }

    data class SetNotificationPrivacy(val notificationPrivacy: NotificationPrivacy) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState =
            state.copy(notificationPrivacy = notificationPrivacy)
    }

    data class SetBlockedContacts(val blockedContacts: List<BlockedContact>) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState =
            state.copy(blockedContacts = blockedContacts)
    }

    data class ToggleReadReceipts(val enabled: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState =
            state.copy(readReceiptsEnabled = enabled)
    }

    data class SetSubScreen(val subScreen: SettingsSubScreen) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState =
            state.copy(subScreen = subScreen)
    }

    data class SetShowPrivacyDialog(val show: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState =
            state.copy(showPrivacyDialog = show)
    }

    data class SetShowQuietHoursDialog(val show: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState =
            state.copy(showQuietHoursDialog = show)
    }
}
