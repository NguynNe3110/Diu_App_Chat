package com.uzuu.diuchat.feature.settings.data.repository

import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl : SettingsRepository {
    private val quietScheduleFlow = MutableStateFlow(
        QuietSchedule(
            enabled = true,
            startMinuteOfDay = 22 * 60 + 30, // 22:30
            endMinuteOfDay = 6 * 60, // 06:00
            timeZoneId = "Asia/Ho_Chi_Minh",
            allowedEmergencyContactIds = setOf("emergency_contact_linh"),
        ),
    )

    private val notificationPrivacyFlow = MutableStateFlow(NotificationPrivacy.FULL_CONTENT)

    private val blockedContactsFlow = MutableStateFlow(
        listOf(
            BlockedContact(
                id = "block_1",
                name = "Quảng cáo Sim Số Đẹp",
                phone = "+84900112233",
                blockedAt = "15/11/2026",
            ),
            BlockedContact(
                id = "block_2",
                name = "Người lạ nhắn tin rác",
                phone = "+84911223344",
                blockedAt = "18/11/2026",
            ),
        ),
    )

    override fun getQuietSchedule(): Flow<QuietSchedule> = quietScheduleFlow.asStateFlow()

    override suspend fun updateQuietSchedule(quietSchedule: QuietSchedule) {
        quietScheduleFlow.value = quietSchedule
    }

    override fun getNotificationPrivacy(): Flow<NotificationPrivacy> = notificationPrivacyFlow.asStateFlow()

    override suspend fun updateNotificationPrivacy(notificationPrivacy: NotificationPrivacy) {
        notificationPrivacyFlow.value = notificationPrivacy
    }

    override fun getBlockedContacts(): Flow<List<BlockedContact>> = blockedContactsFlow.asStateFlow()

    override suspend fun unblockContact(contactId: String) {
        blockedContactsFlow.update { current ->
            current.filterNot { it.id == contactId }
        }
    }

    override suspend fun blockContact(contactId: String, name: String, phone: String) {
        blockedContactsFlow.update { current ->
            current + BlockedContact(
                id = contactId,
                name = name,
                phone = phone,
                blockedAt = "Hôm nay",
            )
        }
    }
}
