package com.uzuu.diuchat.feature.settings.data.repository

import com.uzuu.diuchat.feature.settings.data.datasource.preferences.SettingsPreferencesDataSource
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl(
    private val preferencesDataSource: SettingsPreferencesDataSource? = null,
) : SettingsRepository {

    private val inMemoryThemeModeFlow = MutableStateFlow("SYSTEM")
    private val inMemoryReadReceiptsFlow = MutableStateFlow(true)

    private val inMemoryQuietScheduleFlow = MutableStateFlow(
        QuietSchedule(
            enabled = true,
            startMinuteOfDay = 22 * 60 + 30, // 22:30
            endMinuteOfDay = 6 * 60, // 06:00
            timeZoneId = "Asia/Ho_Chi_Minh",
            allowedEmergencyContactIds = setOf("emergency_contact_linh"),
        ),
    )

    private val inMemoryNotificationPrivacyFlow = MutableStateFlow(NotificationPrivacy.FULL_CONTENT)

    private val inMemoryBlockedContactsFlow = MutableStateFlow(
        SettingsPreferencesDataSource.defaultBlockedContacts(),
    )

    private val inMemoryUserProfileFlow = MutableStateFlow(
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

    override fun getThemeMode(): Flow<String> = preferencesDataSource?.themeModeFlow ?: inMemoryThemeModeFlow.asStateFlow()

    override suspend fun updateThemeMode(themeMode: String) {
        preferencesDataSource?.updateThemeMode(themeMode) ?: run {
            inMemoryThemeModeFlow.value = themeMode
        }
    }

    override fun getReadReceiptsEnabled(): Flow<Boolean> = preferencesDataSource?.readReceiptsFlow ?: inMemoryReadReceiptsFlow.asStateFlow()

    override suspend fun updateReadReceiptsEnabled(enabled: Boolean) {
        preferencesDataSource?.updateReadReceipts(enabled) ?: run {
            inMemoryReadReceiptsFlow.value = enabled
        }
    }

    override fun getQuietSchedule(): Flow<QuietSchedule> = preferencesDataSource?.quietScheduleFlow ?: inMemoryQuietScheduleFlow.asStateFlow()

    override suspend fun updateQuietSchedule(quietSchedule: QuietSchedule) {
        preferencesDataSource?.updateQuietSchedule(quietSchedule) ?: run {
            inMemoryQuietScheduleFlow.value = quietSchedule
        }
    }

    override fun getNotificationPrivacy(): Flow<NotificationPrivacy> = preferencesDataSource?.notificationPrivacyFlow ?: inMemoryNotificationPrivacyFlow.asStateFlow()

    override suspend fun updateNotificationPrivacy(notificationPrivacy: NotificationPrivacy) {
        preferencesDataSource?.updateNotificationPrivacy(notificationPrivacy) ?: run {
            inMemoryNotificationPrivacyFlow.value = notificationPrivacy
        }
    }

    override fun getBlockedContacts(): Flow<List<BlockedContact>> = preferencesDataSource?.blockedContactsFlow ?: inMemoryBlockedContactsFlow.asStateFlow()

    override suspend fun unblockContact(contactId: String) {
        if (preferencesDataSource != null) {
            val current = preferencesDataSource.blockedContactsFlow.first()
            preferencesDataSource.updateBlockedContacts(current.filterNot { it.id == contactId })
        } else {
            inMemoryBlockedContactsFlow.update { current ->
                current.filterNot { it.id == contactId }
            }
        }
    }

    override suspend fun blockContact(contactId: String, name: String, phone: String) {
        val newBlocked = BlockedContact(
            id = contactId,
            name = name,
            phone = phone,
            blockedAt = "Hôm nay",
        )
        if (preferencesDataSource != null) {
            val current = preferencesDataSource.blockedContactsFlow.first()
            preferencesDataSource.updateBlockedContacts(current + newBlocked)
        } else {
            inMemoryBlockedContactsFlow.update { current ->
                current + newBlocked
            }
        }
    }

    override fun getUserProfile(): Flow<UserProfile> = preferencesDataSource?.userProfileFlow ?: inMemoryUserProfileFlow.asStateFlow()

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        preferencesDataSource?.updateUserProfile(userProfile) ?: run {
            inMemoryUserProfileFlow.value = userProfile
        }
    }

    override suspend fun logout() {
        val emptyProfile = UserProfile(
            id = "",
            displayName = "",
            username = "",
            bio = "",
            birthday = "",
            phone = "",
            primaryMood = "Bình yên",
            avatarUrl = null,
        )
        preferencesDataSource?.updateUserProfile(emptyProfile) ?: run {
            inMemoryUserProfileFlow.value = emptyProfile
        }
    }

    override suspend fun deleteAccount(confirmPhone: String) {
        val current = getUserProfile().first()
        require(current.phone == confirmPhone) {
            "Số điện thoại xác nhận không khớp."
        }
        val anonymizedProfile = UserProfile(
            id = current.id,
            displayName = "Người dùng Dịu",
            username = "deleted_user",
            bio = "Tài khoản đã được xoá an toàn.",
            birthday = "",
            phone = "",
            primaryMood = "Bình yên",
            avatarUrl = null,
        )
        preferencesDataSource?.updateUserProfile(anonymizedProfile) ?: run {
            inMemoryUserProfileFlow.value = anonymizedProfile
        }
    }
}
