package com.uzuu.diuchat.feature.settings.data.repository

import com.uzuu.diuchat.feature.settings.data.datasource.api.SettingRemoteDataSource
import com.uzuu.diuchat.feature.settings.data.datasource.preferences.SettingsPreferencesDataSource
import com.uzuu.diuchat.feature.settings.data.mapper.UserProfileMapper
import com.uzuu.diuchat.feature.settings.data.mapper.UserSettingsMapper
import com.uzuu.diuchat.feature.settings.data.mapper.UserSettingsSnapshot
import com.uzuu.diuchat.feature.settings.domain.model.BlockedContact
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile
import com.uzuu.diuchat.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

internal class SettingsRepositoryImpl(
    private val preferencesDataSource: SettingsPreferencesDataSource? = null,
    private val remoteDataSource: SettingRemoteDataSource? = null,
    private val userProfileMapper: UserProfileMapper = UserProfileMapper(),
    private val userSettingsMapper: UserSettingsMapper = UserSettingsMapper(),
) : SettingsRepository {

    private val inMemoryOnboardingCompletedFlow = MutableStateFlow(false)
    private val inMemoryThemeModeFlow = MutableStateFlow("SYSTEM")
    private val inMemoryReadReceiptsFlow = MutableStateFlow(true)
    private val inMemorySoftSendAnimationFlow = MutableStateFlow(true)
    private val inMemoryVibrateOnceFlow = MutableStateFlow(true)
    private val inMemoryEveningGroupDigestFlow = MutableStateFlow(true)

    private val inMemoryQuietScheduleFlow = MutableStateFlow(
        QuietSchedule(
            enabled = true,
            startMinuteOfDay = DEFAULT_QUIET_START_MINUTE,
            endMinuteOfDay = DEFAULT_QUIET_END_MINUTE,
            timeZoneId = "Asia/Ho_Chi_Minh",
            allowedEmergencyContactIds = setOf("emergency_contact_linh"),
        ),
    )

    private val inMemoryNotificationPrivacyFlow = MutableStateFlow(NotificationPrivacy.FULL_CONTENT)
    private val inMemoryNotificationSoundFlow = MutableStateFlow(NotificationSound.WATER_DROP)

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

    @Suppress("MaxLineLength")
    override fun getOnboardingCompleted(): Flow<Boolean> = preferencesDataSource?.onboardingCompletedFlow ?: inMemoryOnboardingCompletedFlow.asStateFlow()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesDataSource?.updateOnboardingCompleted(completed) ?: run {
            inMemoryOnboardingCompletedFlow.value = completed
        }
    }

    override suspend fun refreshFromRemote() {
        syncFromRemote()
    }

    override fun getThemeMode(): Flow<String> = preferencesDataSource?.themeModeFlow ?: inMemoryThemeModeFlow.asStateFlow()

    override suspend fun updateThemeMode(themeMode: String) {
        saveThemeModeLocally(themeMode)
        syncSettingsToRemote()
    }

    @Suppress("MaxLineLength")
    override fun getReadReceiptsEnabled(): Flow<Boolean> = preferencesDataSource?.readReceiptsFlow ?: inMemoryReadReceiptsFlow.asStateFlow()

    override suspend fun updateReadReceiptsEnabled(enabled: Boolean) {
        saveReadReceiptsLocally(enabled)
        syncSettingsToRemote()
    }

    @Suppress("MaxLineLength")
    override fun getSoftSendAnimationEnabled(): Flow<Boolean> = preferencesDataSource?.softSendAnimationFlow ?: inMemorySoftSendAnimationFlow.asStateFlow()

    override suspend fun updateSoftSendAnimationEnabled(enabled: Boolean) {
        saveSoftSendAnimationLocally(enabled)
        syncSettingsToRemote()
    }

    @Suppress("MaxLineLength")
    override fun getNotificationSound(): Flow<NotificationSound> = preferencesDataSource?.notificationSoundFlow ?: inMemoryNotificationSoundFlow.asStateFlow()

    override suspend fun updateNotificationSound(sound: NotificationSound) {
        saveNotificationSoundLocally(sound)
        syncSettingsToRemote()
    }

    @Suppress("MaxLineLength")
    override fun getVibrateOnceEnabled(): Flow<Boolean> = preferencesDataSource?.vibrateOnceFlow ?: inMemoryVibrateOnceFlow.asStateFlow()

    override suspend fun updateVibrateOnceEnabled(enabled: Boolean) {
        saveVibrateOnceLocally(enabled)
        syncSettingsToRemote()
    }

    @Suppress("MaxLineLength")
    override fun getEveningGroupDigestEnabled(): Flow<Boolean> = preferencesDataSource?.eveningGroupDigestFlow ?: inMemoryEveningGroupDigestFlow.asStateFlow()

    override suspend fun updateEveningGroupDigestEnabled(enabled: Boolean) {
        saveEveningGroupDigestLocally(enabled)
        syncSettingsToRemote()
    }

    @Suppress("MaxLineLength")
    override fun getQuietSchedule(): Flow<QuietSchedule> = preferencesDataSource?.quietScheduleFlow ?: inMemoryQuietScheduleFlow.asStateFlow()

    override suspend fun updateQuietSchedule(quietSchedule: QuietSchedule) {
        saveQuietScheduleLocally(quietSchedule)
        syncSettingsToRemote()
    }

    @Suppress("MaxLineLength")
    override fun getNotificationPrivacy(): Flow<NotificationPrivacy> = preferencesDataSource?.notificationPrivacyFlow ?: inMemoryNotificationPrivacyFlow.asStateFlow()

    override suspend fun updateNotificationPrivacy(notificationPrivacy: NotificationPrivacy) {
        saveNotificationPrivacyLocally(notificationPrivacy)
        syncSettingsToRemote()
    }

    @Suppress("MaxLineLength")
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
        saveUserProfileLocally(userProfile)
        runRemoteSafely {
            remoteDataSource?.upsertUserProfile(userProfileMapper.toDto(userProfile))
        }
    }

    override suspend fun logout() {
        remoteDataSource?.signOut()
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
        saveUserProfileLocally(emptyProfile)
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
        runRemoteSafely {
            remoteDataSource?.upsertUserProfile(userProfileMapper.toDto(anonymizedProfile))
        }
        remoteDataSource?.signOut()
        saveUserProfileLocally(anonymizedProfile)
    }

    suspend fun syncFromRemote() {
        val bundle = runRemoteSafely {
            remoteDataSource?.fetchUserBundle()
        } ?: return

        bundle.profile?.let { profileDto ->
            saveUserProfileLocally(userProfileMapper.toDomain(profileDto))
        }
        bundle.settings?.let { settingsDto ->
            settingsDto.themeMode?.let { remoteTheme ->
                val currentLocalTheme = getThemeMode().first()
                val shouldKeepSystem = remoteTheme.equals("LIGHT", ignoreCase = true) &&
                    currentLocalTheme.equals("SYSTEM", ignoreCase = true)
                if (!shouldKeepSystem) {
                    saveThemeModeLocally(remoteTheme)
                }
            }
            settingsDto.readReceiptsEnabled?.let { saveReadReceiptsLocally(it) }
            settingsDto.softSendAnimationEnabled?.let { saveSoftSendAnimationLocally(it) }
            settingsDto.vibrateOnceEnabled?.let { saveVibrateOnceLocally(it) }
            settingsDto.eveningGroupDigestEnabled?.let { saveEveningGroupDigestLocally(it) }
            saveNotificationPrivacyLocally(userSettingsMapper.toNotificationPrivacy(settingsDto))
            saveNotificationSoundLocally(userSettingsMapper.toNotificationSound(settingsDto))
            saveQuietScheduleLocally(userSettingsMapper.toQuietSchedule(settingsDto))
        }
    }

    private suspend fun syncSettingsToRemote() {
        val remote = remoteDataSource ?: return
        val userId = getUserProfile().first().id

        val dto = userSettingsMapper.toDto(
            userId = userId,
            snapshot = UserSettingsSnapshot(
                themeMode = getThemeMode().first(),
                readReceiptsEnabled = getReadReceiptsEnabled().first(),
                softSendAnimationEnabled = getSoftSendAnimationEnabled().first(),
                vibrateOnceEnabled = getVibrateOnceEnabled().first(),
                eveningGroupDigestEnabled = getEveningGroupDigestEnabled().first(),
                notificationPrivacy = getNotificationPrivacy().first(),
                notificationSound = getNotificationSound().first(),
                quietSchedule = getQuietSchedule().first(),
            ),
        )
        runRemoteSafely {
            remote.upsertUserSettings(dto)
        }
    }

    private suspend fun saveThemeModeLocally(themeMode: String) {
        preferencesDataSource?.updateThemeMode(themeMode) ?: run {
            inMemoryThemeModeFlow.value = themeMode
        }
    }

    private suspend fun saveReadReceiptsLocally(enabled: Boolean) {
        preferencesDataSource?.updateReadReceipts(enabled) ?: run {
            inMemoryReadReceiptsFlow.value = enabled
        }
    }

    private suspend fun saveSoftSendAnimationLocally(enabled: Boolean) {
        preferencesDataSource?.updateSoftSendAnimation(enabled) ?: run {
            inMemorySoftSendAnimationFlow.value = enabled
        }
    }

    private suspend fun saveVibrateOnceLocally(enabled: Boolean) {
        preferencesDataSource?.updateVibrateOnce(enabled) ?: run {
            inMemoryVibrateOnceFlow.value = enabled
        }
    }

    private suspend fun saveEveningGroupDigestLocally(enabled: Boolean) {
        preferencesDataSource?.updateEveningGroupDigest(enabled) ?: run {
            inMemoryEveningGroupDigestFlow.value = enabled
        }
    }

    private suspend fun saveNotificationSoundLocally(sound: NotificationSound) {
        preferencesDataSource?.updateNotificationSound(sound) ?: run {
            inMemoryNotificationSoundFlow.value = sound
        }
    }

    private suspend fun saveQuietScheduleLocally(quietSchedule: QuietSchedule) {
        preferencesDataSource?.updateQuietSchedule(quietSchedule) ?: run {
            inMemoryQuietScheduleFlow.value = quietSchedule
        }
    }

    private suspend fun saveNotificationPrivacyLocally(privacy: NotificationPrivacy) {
        preferencesDataSource?.updateNotificationPrivacy(privacy) ?: run {
            inMemoryNotificationPrivacyFlow.value = privacy
        }
    }

    private suspend fun saveUserProfileLocally(userProfile: UserProfile) {
        preferencesDataSource?.updateUserProfile(userProfile) ?: run {
            inMemoryUserProfileFlow.value = userProfile
        }
    }

    private inline fun <T> runRemoteSafely(block: () -> T): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Throwable) {
        null
    }

    private companion object {
        const val DEFAULT_QUIET_START_MINUTE = 22 * 60 + 30
        const val DEFAULT_QUIET_END_MINUTE = 6 * 60
    }
}
