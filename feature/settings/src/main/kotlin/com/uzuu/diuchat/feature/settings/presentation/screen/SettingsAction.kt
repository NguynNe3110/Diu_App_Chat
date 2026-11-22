package com.uzuu.diuchat.feature.settings.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.settings.domain.model.AccountDeletionState
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile

sealed class SettingsAction : BaseAction<SettingsUiState> {
    data class SetUserProfile(val userProfile: UserProfile) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(userProfile = userProfile)
    }

    data class SetQuietSchedule(val quietSchedule: QuietSchedule) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(quietSchedule = quietSchedule)
    }

    data class SetNotificationPrivacy(val notificationPrivacy: NotificationPrivacy) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(notificationPrivacy = notificationPrivacy)
    }

    data class SetNotificationSound(val notificationSound: NotificationSound) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(notificationSound = notificationSound)
    }

    data class SetBlockedContacts(val blockedContacts: List<BlockedContact>) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(blockedContacts = blockedContacts)
    }

    data class ToggleReadReceipts(val enabled: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(readReceiptsEnabled = enabled)
    }

    data class ToggleSoftSendAnimation(val enabled: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(softSendAnimationEnabled = enabled)
    }

    data class ToggleVibrateOnce(val enabled: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(vibrateOnceEnabled = enabled)
    }

    data class ToggleEveningGroupDigest(val enabled: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(eveningGroupDigestEnabled = enabled)
    }

    data class SetThemeMode(val themeMode: String) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(themeMode = themeMode)
    }

    data class SetSubScreen(val subScreen: SettingsSubScreen) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(subScreen = subScreen)
    }

    data class SetShowPrivacyDialog(val show: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(showPrivacyDialog = show)
    }

    data class SetShowQuietHoursDialog(val show: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(showQuietHoursDialog = show)
    }

    data class SetShowEditProfileSheet(val show: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(showEditProfileSheet = show)
    }

    data class SetShowDeleteAccountDialog(val show: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(showDeleteAccountDialog = show)
    }

    data class SetShowLogoutConfirmDialog(val show: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(
            showLogoutConfirmDialog = show,
            logoutError = if (show) null else state.logoutError,
        )
    }

    data class SetLogoutError(val error: String?) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(logoutError = error)
    }

    data class SetAccountDeletionState(val deletionState: AccountDeletionState) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(accountDeletionState = deletionState)
    }

    data class SetLoggedOut(val loggedOut: Boolean) : SettingsAction() {
        override fun reduce(state: SettingsUiState): SettingsUiState = state.copy(isLoggedOut = loggedOut)
    }
}
