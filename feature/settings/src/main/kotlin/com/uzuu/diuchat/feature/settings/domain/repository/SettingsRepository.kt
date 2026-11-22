package com.uzuu.diuchat.feature.settings.domain.repository

import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Suppress("ComplexInterface", "TooManyFunctions")
interface SettingsRepository {
    fun getQuietSchedule(): Flow<QuietSchedule>
    suspend fun updateQuietSchedule(quietSchedule: QuietSchedule)
    fun getNotificationPrivacy(): Flow<NotificationPrivacy>
    suspend fun updateNotificationPrivacy(notificationPrivacy: NotificationPrivacy)
    fun getBlockedContacts(): Flow<List<BlockedContact>>
    suspend fun unblockContact(contactId: String)
    suspend fun blockContact(contactId: String, name: String, phone: String)
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateUserProfile(userProfile: UserProfile)
    suspend fun logout()
    suspend fun deleteAccount(confirmPhone: String)

    fun getOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun refreshFromRemote()

    fun getThemeMode(): Flow<String>
    suspend fun updateThemeMode(themeMode: String)
    fun getReadReceiptsEnabled(): Flow<Boolean>
    suspend fun updateReadReceiptsEnabled(enabled: Boolean)
    fun getSoftSendAnimationEnabled(): Flow<Boolean>
    suspend fun updateSoftSendAnimationEnabled(enabled: Boolean)
    fun getNotificationSound(): Flow<NotificationSound>
    suspend fun updateNotificationSound(sound: NotificationSound)
    fun getVibrateOnceEnabled(): Flow<Boolean> = flowOf(true)
    suspend fun updateVibrateOnceEnabled(enabled: Boolean) {}
    fun getEveningGroupDigestEnabled(): Flow<Boolean> = flowOf(true)
    suspend fun updateEveningGroupDigestEnabled(enabled: Boolean) {}
}
