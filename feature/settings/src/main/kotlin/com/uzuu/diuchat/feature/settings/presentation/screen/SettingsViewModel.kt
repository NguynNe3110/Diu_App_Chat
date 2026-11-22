package com.uzuu.diuchat.feature.settings.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.settings.domain.model.AccountDeletionState
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.usecase.DeleteAccountUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetBlockedContactsUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetEveningGroupDigestEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetNotificationSoundUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetReadReceiptsEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetSoftSendAnimationEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetThemeModeUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.GetVibrateOnceEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.LogoutUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UnblockContactUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateEveningGroupDigestEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationPrivacyUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateNotificationSoundUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateProfileUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateQuietScheduleUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateReadReceiptsEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateSoftSendAnimationEnabledUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateThemeModeUseCase
import com.uzuu.diuchat.feature.settings.domain.usecase.UpdateVibrateOnceEnabledUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class SettingsViewModel(
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getBlockedContactsUseCase: GetBlockedContactsUseCase,
    private val getEveningGroupDigestEnabledUseCase: GetEveningGroupDigestEnabledUseCase,
    private val getNotificationPrivacyUseCase: GetNotificationPrivacyUseCase,
    private val getNotificationSoundUseCase: GetNotificationSoundUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getQuietScheduleUseCase: GetQuietScheduleUseCase,
    private val getReadReceiptsEnabledUseCase: GetReadReceiptsEnabledUseCase,
    private val getSoftSendAnimationEnabledUseCase: GetSoftSendAnimationEnabledUseCase,
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val getVibrateOnceEnabledUseCase: GetVibrateOnceEnabledUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val unblockContactUseCase: UnblockContactUseCase,
    private val updateEveningGroupDigestEnabledUseCase: UpdateEveningGroupDigestEnabledUseCase,
    private val updateNotificationPrivacyUseCase: UpdateNotificationPrivacyUseCase,
    private val updateNotificationSoundUseCase: UpdateNotificationSoundUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateQuietScheduleUseCase: UpdateQuietScheduleUseCase,
    private val updateReadReceiptsEnabledUseCase: UpdateReadReceiptsEnabledUseCase,
    private val updateSoftSendAnimationEnabledUseCase: UpdateSoftSendAnimationEnabledUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase,
    private val updateVibrateOnceEnabledUseCase: UpdateVibrateOnceEnabledUseCase,
) : BaseViewModel<SettingsUiState, SettingsAction>(SettingsUiState()) {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        val coreFlow = combine(
            getProfileUseCase(),
            getQuietScheduleUseCase(),
            getNotificationPrivacyUseCase(),
            getNotificationSoundUseCase(),
            getBlockedContactsUseCase(),
        ) { profile, quiet, privacy, sound, blocked ->
            CoreSettingsBundle(profile, quiet, privacy, sound, blocked)
        }
        val togglesFlow = combine(
            getReadReceiptsEnabledUseCase(),
            getSoftSendAnimationEnabledUseCase(),
            getVibrateOnceEnabledUseCase(),
            getEveningGroupDigestEnabledUseCase(),
            getThemeModeUseCase(),
        ) { readReceipts, softSend, vibrateOnce, groupDigest, themeMode ->
            ToggleSettingsBundle(readReceipts, softSend, vibrateOnce, groupDigest, themeMode)
        }

        combine(coreFlow, togglesFlow) { core, toggles ->
            SettingsAction.SetLoadedSettings(
                userProfile = core.profile,
                quietSchedule = core.quietSchedule,
                notificationPrivacy = core.notificationPrivacy,
                notificationSound = core.notificationSound,
                blockedContacts = core.blockedContacts,
                readReceiptsEnabled = toggles.readReceiptsEnabled,
                softSendAnimationEnabled = toggles.softSendAnimationEnabled,
                vibrateOnceEnabled = toggles.vibrateOnceEnabled,
                eveningGroupDigestEnabled = toggles.eveningGroupDigestEnabled,
                themeMode = toggles.themeMode,
            )
        }.onEach { action ->
            sendAction(action)
        }.launchIn(viewModelScope)
    }

    private data class CoreSettingsBundle(
        val profile: com.uzuu.diuchat.feature.settings.domain.model.UserProfile,
        val quietSchedule: com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule,
        val notificationPrivacy: NotificationPrivacy,
        val notificationSound: NotificationSound,
        val blockedContacts: List<com.uzuu.diuchat.feature.settings.domain.model.BlockedContact>,
    )

    private data class ToggleSettingsBundle(
        val readReceiptsEnabled: Boolean,
        val softSendAnimationEnabled: Boolean,
        val vibrateOnceEnabled: Boolean,
        val eveningGroupDigestEnabled: Boolean,
        val themeMode: String,
    )

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

    fun stepQuietStartTime(deltaMinutes: Int) {
        val updated = uiStateFlow.value.quietSchedule.stepStartMinutes(deltaMinutes)
        viewModelScope.launch {
            updateQuietScheduleUseCase(updated)
        }
    }

    fun stepQuietEndTime(deltaMinutes: Int) {
        val updated = uiStateFlow.value.quietSchedule.stepEndMinutes(deltaMinutes)
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

    fun toggleSoftSendAnimation(enabled: Boolean) {
        viewModelScope.launch {
            updateSoftSendAnimationEnabledUseCase(enabled)
        }
    }

    fun toggleVibrateOnce(enabled: Boolean) {
        viewModelScope.launch {
            updateVibrateOnceEnabledUseCase(enabled)
        }
    }

    fun toggleEveningGroupDigest(enabled: Boolean) {
        viewModelScope.launch {
            updateEveningGroupDigestEnabledUseCase(enabled)
        }
    }

    fun setNotificationSound(sound: NotificationSound) {
        viewModelScope.launch {
            updateNotificationSoundUseCase(sound)
        }
    }

    fun toggleQuietRepeatDay(day: Int) {
        val current = uiStateFlow.value.quietSchedule
        val updatedDays = if (day in current.repeatDays) {
            current.repeatDays - day
        } else {
            current.repeatDays + day
        }
        val updated = current.copy(repeatDays = updatedDays)
        viewModelScope.launch {
            updateQuietScheduleUseCase(updated)
        }
    }

    fun toggleEmergencyContact(contactId: String) {
        val current = uiStateFlow.value.quietSchedule
        val updatedContacts = if (contactId in current.allowedEmergencyContactIds) {
            current.allowedEmergencyContactIds - contactId
        } else {
            current.allowedEmergencyContactIds + contactId
        }
        val updated = current.copy(allowedEmergencyContactIds = updatedContacts)
        viewModelScope.launch {
            updateQuietScheduleUseCase(updated)
        }
    }

    fun unblockContact(contactId: String) {
        viewModelScope.launch {
            unblockContactUseCase(contactId)
        }
    }

    fun navigateToSubScreen(subScreen: SettingsSubScreen) {
        sendAction(SettingsAction.SetSubScreen(subScreen))
    }

    fun navigateToBlockedContacts() {
        sendAction(SettingsAction.SetSubScreen(SettingsSubScreen.BLOCKED_CONTACTS))
    }

    fun navigateBackToOverview() {
        sendAction(SettingsAction.SetSubScreen(SettingsSubScreen.PROFILE_OVERVIEW))
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

    fun updatePrimaryMoodQuickly(moodName: String) {
        val current = uiStateFlow.value.userProfile
        val updated = current.copy(primaryMood = moodName)
        viewModelScope.launch {
            updateProfileUseCase(updated)
        }
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
            if (uiStateFlow.value.subScreen == SettingsSubScreen.EDIT_PROFILE) {
                sendAction(SettingsAction.SetSubScreen(SettingsSubScreen.PROFILE_OVERVIEW))
            }
        }
    }

    fun showLogoutConfirm(show: Boolean) {
        sendAction(SettingsAction.SetShowLogoutConfirmDialog(show))
    }

    @Suppress("TooGenericExceptionCaught")
    fun logout() {
        viewModelScope.launch {
            try {
                sendAction(SettingsAction.SetLogoutError(null))
                logoutUseCase()
                sendAction(SettingsAction.SetShowLogoutConfirmDialog(false))
                sendAction(SettingsAction.SetLoggedOut(true))
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                sendAction(
                    SettingsAction.SetLogoutError(
                        e.message?.takeIf { it.isNotBlank() }
                            ?: "Không thể gỡ đăng ký thiết bị. Kiểm tra kết nối mạng và thử lại.",
                    ),
                )
            }
        }
    }

    fun consumeLoggedOut() {
        sendAction(SettingsAction.SetLoggedOut(false))
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                sendAction(SettingsAction.SetAccountDeletionState(AccountDeletionState.Failed(e.message ?: "Lỗi")))
            }
        }
    }
}
