package com.uzuu.diuchat.feature.settings.data.repository

import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
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

    private val userProfileFlow = MutableStateFlow(
        UserProfile(
            id = "user_me",
            displayName = "Minh",
            username = "minh_diu",
            bio = "Tìm kiếm những điều dịu dàng và bình yên mỗi ngày.",
            birthday = "15/08/2000",
            phone = "+84901234567",
            primaryMood = "Bình yên",
            avatarUrl = null,
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

    override fun getUserProfile(): Flow<UserProfile> = userProfileFlow.asStateFlow()

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        userProfileFlow.value = userProfile
    }

    override suspend fun logout() {
        // Clear active session / memory cache
    }

    override suspend fun deleteAccount(confirmPhone: String) {
        val current = userProfileFlow.value
        if (current.phone == confirmPhone || confirmPhone.isNotBlank()) {
            // Anonymize user profile and mark tombstone
            userProfileFlow.value = UserProfile(
                id = current.id,
                displayName = "Người dùng Dịu",
                username = "deleted_user",
                bio = "Tài khoản đã được xoá an toàn.",
                birthday = "",
                phone = "",
                primaryMood = "Bình yên",
                avatarUrl = null,
            )
        } else {
            throw IllegalArgumentException("Số điện thoại xác nhận không khớp.")
        }
    }
}
