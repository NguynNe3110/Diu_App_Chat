package com.uzuu.diuchat.feature.settings.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule

data class SettingsUiState(
    val quietSchedule: QuietSchedule = QuietSchedule(),
    val notificationPrivacy: NotificationPrivacy = NotificationPrivacy.FULL_CONTENT,
    val blockedContacts: List<BlockedContact> = emptyList(),
    val readReceiptsEnabled: Boolean = true,
    val subScreen: SettingsSubScreen = SettingsSubScreen.OVERVIEW,
    val showPrivacyDialog: Boolean = false,
    val showQuietHoursDialog: Boolean = false,
) : BaseState
