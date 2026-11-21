package com.uzuu.diuchat.feature.settings.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.usecase.GetBlockedContactsUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UnblockContactUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateQuietScheduleUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getBlockedContactsUseCase: GetBlockedContactsUseCase,
    private val getNotificationPrivacyUseCase: GetNotificationPrivacyUseCase,
    private val getQuietScheduleUseCase: GetQuietScheduleUseCase,
    private val unblockContactUseCase: UnblockContactUseCase,
    private val updateNotificationPrivacyUseCase: UpdateNotificationPrivacyUseCase,
    private val updateQuietScheduleUseCase: UpdateQuietScheduleUseCase,
) : BaseViewModel<SettingsUiState, SettingsAction>(SettingsUiState()) {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        getQuietScheduleUseCase()
            .onEach { sendAction(SettingsAction.SetQuietSchedule(it)) }
            .launchIn(viewModelScope)

        getNotificationPrivacyUseCase()
            .onEach { sendAction(SettingsAction.SetNotificationPrivacy(it)) }
            .launchIn(viewModelScope)

        getBlockedContactsUseCase()
            .onEach { sendAction(SettingsAction.SetBlockedContacts(it)) }
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
        sendAction(SettingsAction.ToggleReadReceipts(enabled))
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
}
