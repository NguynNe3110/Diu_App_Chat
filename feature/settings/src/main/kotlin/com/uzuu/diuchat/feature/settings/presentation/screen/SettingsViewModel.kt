package com.uzuu.diuchat.feature.settings.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.settings.domain.model.AccountDeletionState
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.usecase.DeleteAccountUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetBlockedContactsUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetReadReceiptsEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetThemeModeUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.LogoutUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UnblockContactUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateReadReceiptsEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateThemeModeUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getBlockedContactsUseCase: GetBlockedContactsUseCase,
    private val getNotificationPrivacyUseCase: GetNotificationPrivacyUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getQuietScheduleUseCase: GetQuietScheduleUseCase,
    private val getReadReceiptsEnabledUseCase: GetReadReceiptsEnabledUseCase,
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val unblockContactUseCase: UnblockContactUseCase,
    private val updateNotificationPrivacyUseCase: UpdateNotificationPrivacyUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateQuietScheduleUseCase: UpdateQuietScheduleUseCase,
    private val updateReadReceiptsEnabledUseCase: UpdateReadReceiptsEnabledUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase,
) : BaseViewModel<SettingsUiState, SettingsAction>(SettingsUiState()) {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        getProfileUseCase()
            .onEach { sendAction(SettingsAction.SetUserProfile(it)) }
            .launchIn(viewModelScope)

        getQuietScheduleUseCase()
            .onEach { sendAction(SettingsAction.SetQuietSchedule(it)) }
            .launchIn(viewModelScope)

        getNotificationPrivacyUseCase()
            .onEach { sendAction(SettingsAction.SetNotificationPrivacy(it)) }
            .launchIn(viewModelScope)

        getBlockedContactsUseCase()
            .onEach { sendAction(SettingsAction.SetBlockedContacts(it)) }
            .launchIn(viewModelScope)

        getReadReceiptsEnabledUseCase()
            .onEach { sendAction(SettingsAction.ToggleReadReceipts(it)) }
            .launchIn(viewModelScope)

        getThemeModeUseCase()
            .onEach { sendAction(SettingsAction.SetThemeMode(it)) }
            .launchIn(viewModelScope)
    }

    fun toggleQuietHours(enabled: Boolean) {
        val current = uiStateFlow.value.quietSchedule
        val updated = current.copy(enabled = enabled)
        viewModelScope.launch {
            updateQuietScheduleUseCase(updated)
        }
    }

    fun updateQuietTimeRange(startMinute: Int, endMinute: Int) {
        val current = uiStateFlow.value.quietSchedule
        val updated = current.copy(startMinuteOfDay = startMinute, endMinuteOfDay = endMinute)
        viewModelScope.launch {
            updateQuietScheduleUseCase(updated)
        }
    }

    fun setNotificationPrivacy(privacy: NotificationPrivacy) {
        viewModelScope.launch {
            updateNotificationPrivacyUseCase(privacy)
            sendAction(SettingsAction.SetShowPrivacyDialog(false))
        }
    }

    fun toggleReadReceipts(enabled: Boolean) {
        viewModelScope.launch {
            updateReadReceiptsEnabledUseCase(enabled)
        }
    }

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            updateThemeModeUseCase(themeMode)
        }
    }

    fun unblockContact(contactId: String) {
        viewModelScope.launch {
            unblockContactUseCase(contactId)
        }
    }

    fun navigateToBlockedContacts() {
        sendAction(SettingsAction.SetSubScreen(SettingsSubScreen.BLOCKED_CONTACTS))
    }

    fun navigateBackToOverview() {
        sendAction(SettingsAction.SetSubScreen(SettingsSubScreen.OVERVIEW))
    }

    fun showPrivacyDialog(show: Boolean) {
        sendAction(SettingsAction.SetShowPrivacyDialog(show))
    }

    fun showQuietHoursDialog(show: Boolean) {
        sendAction(SettingsAction.SetShowQuietHoursDialog(show))
    }

    fun showEditProfile(show: Boolean) {
        sendAction(SettingsAction.SetShowEditProfileSheet(show))
    }

    fun updateProfile(
        displayName: String,
        bio: String,
        birthday: String,
        primaryMood: String,
        avatarUrl: String? = uiStateFlow.value.userProfile.avatarUrl,
    ) {
        val current = uiStateFlow.value.userProfile
        val updated = current.copy(
            displayName = displayName,
            bio = bio,
            birthday = birthday,
            primaryMood = primaryMood,
            avatarUrl = avatarUrl,
        )
        viewModelScope.launch {
            updateProfileUseCase(updated)
            sendAction(SettingsAction.SetShowEditProfileSheet(false))
        }
    }

    fun showLogoutConfirm(show: Boolean) {
        sendAction(SettingsAction.SetShowLogoutConfirmDialog(show))
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            sendAction(SettingsAction.SetShowLogoutConfirmDialog(false))
            sendAction(SettingsAction.SetLoggedOut(true))
        }
    }

    fun showDeleteAccount(show: Boolean) {
        sendAction(SettingsAction.SetShowDeleteAccountDialog(show))
    }

    @Suppress("TooGenericExceptionCaught")
    fun deleteAccount(confirmPhone: String) {
        viewModelScope.launch {
            try {
                sendAction(SettingsAction.SetAccountDeletionState(AccountDeletionState.Processing))
                deleteAccountUseCase(confirmPhone)
                sendAction(SettingsAction.SetAccountDeletionState(AccountDeletionState.Completed))
                sendAction(SettingsAction.SetShowDeleteAccountDialog(false))
                sendAction(SettingsAction.SetLoggedOut(true))
            } catch (e: Exception) {
                sendAction(SettingsAction.SetAccountDeletionState(AccountDeletionState.Failed(e.message ?: "Lỗi")))
            }
        }
    }
}
