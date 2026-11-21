package com.uzuu.diuchat.feature.settings.domain.repository

import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getQuietSchedule(): Flow<QuietSchedule>
    suspend fun updateQuietSchedule(quietSchedule: QuietSchedule)
    fun getNotificationPrivacy(): Flow<NotificationPrivacy>
    suspend fun updateNotificationPrivacy(notificationPrivacy: NotificationPrivacy)
    fun getBlockedContacts(): Flow<List<BlockedContact>>
    suspend fun unblockContact(contactId: String)
    suspend fun blockContact(contactId: String, name: String, phone: String)
}
