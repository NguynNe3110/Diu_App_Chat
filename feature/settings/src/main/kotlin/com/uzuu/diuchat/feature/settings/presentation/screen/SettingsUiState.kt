package com.uzuu.diuchat.feature.settings.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.settings.domain.model.AccountDeletionState
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile

data class SettingsUiState(
    val userProfile: UserProfile = UserProfile(),
    val quietSchedule: QuietSchedule = QuietSchedule(),
    val notificationPrivacy: NotificationPrivacy = NotificationPrivacy.FULL_CONTENT,
    val blockedContacts: List<BlockedContact> = emptyList(),
    val readReceiptsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM",
    val subScreen: SettingsSubScreen = SettingsSubScreen.OVERVIEW,
    val showPrivacyDialog: Boolean = false,
    val showQuietHoursDialog: Boolean = false,
    val showEditProfileSheet: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false,
    val accountDeletionState: AccountDeletionState = AccountDeletionState.Idle,
    val isLoggedOut: Boolean = false,
) : BaseState
