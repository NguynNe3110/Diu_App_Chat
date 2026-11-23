package com.uzuu.diuchat.feature.settings.data.datasource.api.service

import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import com.uzuu.diuchat.feature.settings.domain.model.NotificationPrivacy
import com.uzuu.diuchat.feature.settings.domain.model.NotificationSound
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.math.roundToInt

internal class SettingsSupabaseService(
    private val supabaseClient: SupabaseClient?,
    private val pushRegistration: PushRegistration? = null,
) {
    fun currentUserId(): String? = supabaseClient?.auth?.currentUserOrNull()?.id

    suspend fun fetchUserProfile(): UserProfileDto? {
        val client = supabaseClient ?: return null
        val user = client.auth.currentUserOrNull() ?: return null
        val remote = client.postgrest
            .from(TABLE_USER_PROFILES)
            .select {
                filter {
                    eq("id", user.id)
                }
            }.decodeSingleOrNull<UserProfileDto>()

        val googleName = (user.userMetadata?.get("full_name") as? JsonPrimitive)?.contentOrNull
            ?: (user.userMetadata?.get("name") as? JsonPrimitive)?.contentOrNull

        return if (remote == null) {
            googleName?.let { name ->
                UserProfileDto(
                    id = user.id,
                    displayName = name,
                    avatarUrl = null,
                )
            }
        } else {
            val resolvedName = if (remote.displayName.isBlank() || remote.displayName == "Người thương") {
                googleName ?: remote.displayName
            } else {
                remote.displayName
            }
            val appAvatar = remote.avatarUrl?.takeUnless { url ->
                url.contains("googleusercontent.com", ignoreCase = true)
            }
            remote.copy(
                displayName = resolvedName,
                avatarUrl = appAvatar,
            )
        }
    }

    suspend fun upsertUserProfile(profile: UserProfileDto) {
        val client = supabaseClient ?: return
        client.postgrest
            .from(TABLE_USER_PROFILES)
            .upsert(profile)
        runCatching {
            val globalChannel =
                client.realtime.subscriptions["realtime:$CHANNEL_GLOBAL_SYNC"]
                    ?: client.channel(CHANNEL_GLOBAL_SYNC)
            globalChannel.broadcast(
                event = EVENT_PROFILE_UPDATED,
                message =
                buildJsonObject {
                    put(COL_USER_ID, profile.id)
                },
            )
        }
    }

    suspend fun fetchUserSettings(): UserSettingsDto? {
        val client = supabaseClient ?: return null
        val userId = client.auth.currentUserOrNull()?.id ?: return null

        val settingsRow = runCatching {
            client.postgrest
                .from(TABLE_USER_SETTINGS)
                .select {
                    filter { eq(COL_USER_ID, userId) }
                }.decodeSingleOrNull<RemoteUserSettingsRow>()
        }.getOrNull()

        val quietRow = runCatching {
            client.postgrest
                .from(TABLE_QUIET_SCHEDULES)
                .select {
                    filter { eq(COL_USER_ID, userId) }
                }.decodeSingleOrNull<RemoteQuietScheduleRow>()
        }.getOrNull()

        return if (settingsRow == null && quietRow == null) {
            null
        } else {
            buildUserSettingsDto(userId, settingsRow, quietRow)
        }
    }

    private fun buildUserSettingsDto(
        userId: String,
        settingsRow: RemoteUserSettingsRow?,
        quietRow: RemoteQuietScheduleRow?,
    ): UserSettingsDto = UserSettingsDto(
        userId = userId,
        themeMode = settingsRow?.let { if (it.darkMode) "DARK" else "LIGHT" },
        readReceiptsEnabled = null,
        softSendAnimationEnabled = null,
        vibrateOnceEnabled = settingsRow?.vibrationEnabled,
        eveningGroupDigestEnabled = settingsRow?.groupDigest,
        notificationPrivacy = mapRemoteDisplayToPrivacy(settingsRow?.notifDisplay),
        notificationSound = mapRemoteSoundToDomain(settingsRow?.notifSound),
        quietHoursEnabled = quietRow?.isEnabled ?: true,
        quietHoursStartMinute = quietRow?.let { hoursToMinutes(it.startHour) }
            ?: UserSettingsDto.DEFAULT_QUIET_START_MINUTE,
        quietHoursEndMinute = quietRow?.let { hoursToMinutes(it.endHour) }
            ?: UserSettingsDto.DEFAULT_QUIET_END_MINUTE,
        quietHoursTimezone = settingsRow?.timezone ?: UserSettingsDto.DEFAULT_TIMEZONE,
        quietHoursAllowedContacts = quietRow?.emergencyAllowed ?: emptyList(),
        quietHoursRepeatDays = quietRow?.activeDays?.ifEmpty { UserSettingsDto.DEFAULT_REPEAT_DAYS }
            ?: UserSettingsDto.DEFAULT_REPEAT_DAYS,
    )

    suspend fun upsertUserSettings(settings: UserSettingsDto) {
        val client = supabaseClient ?: return
        val userId = client.auth.currentUserOrNull()?.id
            ?: settings.userId.takeIf { it.isNotBlank() }
            ?: return

        val settingsPayload = buildJsonObject {
            put("notif_display", mapPrivacyToRemoteDisplay(settings.notificationPrivacy))
            put("notif_sound", mapSoundToRemote(settings.notificationSound))
            put("timezone", settings.quietHoursTimezone)
            settings.vibrateOnceEnabled?.let { put("vibration_enabled", it) }
            settings.eveningGroupDigestEnabled?.let { put("group_digest", it) }
            settings.themeMode?.let { put("dark_mode", it.equals("DARK", ignoreCase = true)) }
        }
        client.postgrest
            .from(TABLE_USER_SETTINGS)
            .update(settingsPayload) {
                filter { eq(COL_USER_ID, userId) }
            }

        val validEmergencyUuids = settings.quietHoursAllowedContacts.filter(::isValidUuid)
        val quietPayload = buildJsonObject {
            put(COL_USER_ID, userId)
            put("is_enabled", settings.quietHoursEnabled)
            put("start_hour", minutesToHours(settings.quietHoursStartMinute))
            put("end_hour", minutesToHours(settings.quietHoursEndMinute))
            put(
                "active_days",
                JsonArray(settings.quietHoursRepeatDays.map { JsonPrimitive(it) }),
            )
            put(
                "emergency_allowed",
                JsonArray(validEmergencyUuids.map { JsonPrimitive(it) }),
            )
        }
        client.postgrest
            .from(TABLE_QUIET_SCHEDULES)
            .upsert(quietPayload) {
                onConflict = COL_USER_ID
            }
    }

    suspend fun signOut() {
        val registration = pushRegistration
        if (registration != null) {
            registration.signOut()
        } else {
            val client = supabaseClient ?: return
            client.auth.signOut()
        }
    }

    private fun mapRemoteDisplayToPrivacy(value: String?): String = when (value?.lowercase()) {
        "color" -> NotificationPrivacy.MOOD_COLOR_ONLY.name
        "name" -> NotificationPrivacy.SENDER_NAME.name
        "full" -> NotificationPrivacy.FULL_CONTENT.name
        else -> NotificationPrivacy.FULL_CONTENT.name
    }

    private fun mapPrivacyToRemoteDisplay(privacy: String): String = when (privacy) {
        NotificationPrivacy.MOOD_COLOR_ONLY.name -> "color"
        NotificationPrivacy.SENDER_NAME.name -> "name"
        else -> "full"
    }

    private fun mapRemoteSoundToDomain(value: String?): String = when (value?.lowercase()) {
        "giot" -> NotificationSound.WATER_DROP.name
        "chuong" -> NotificationSound.WIND_CHIME.name
        "la" -> NotificationSound.FALLING_LEAF.name
        "im" -> NotificationSound.SILENT.name
        else -> NotificationSound.WATER_DROP.name
    }

    private fun mapSoundToRemote(sound: String): String = when (sound) {
        NotificationSound.WIND_CHIME.name -> "chuong"
        NotificationSound.FALLING_LEAF.name -> "la"
        NotificationSound.SILENT.name -> "im"
        else -> "giot"
    }

    private fun hoursToMinutes(hours: Double): Int = (hours * MINUTES_PER_HOUR).roundToInt().coerceIn(0, MAX_MINUTE_OF_DAY)

    private fun minutesToHours(minutes: Int): Double {
        val rawHours = minutes.toDouble() / MINUTES_PER_HOUR
        return (rawHours * DECIMAL_SCALE).roundToInt() / DECIMAL_SCALE
    }

    private fun isValidUuid(value: String): Boolean = runCatching { UUID.fromString(value) }.isSuccess

    @Serializable
    private data class RemoteUserSettingsRow(
        @SerialName("user_id")
        val userId: String,
        @SerialName("timezone")
        val timezone: String = UserSettingsDto.DEFAULT_TIMEZONE,
        @SerialName("notif_display")
        val notifDisplay: String = "color",
        @SerialName("notif_sound")
        val notifSound: String = "giot",
        @SerialName("vibration_enabled")
        val vibrationEnabled: Boolean = true,
        @SerialName("group_digest")
        val groupDigest: Boolean = true,
        @SerialName("dark_mode")
        val darkMode: Boolean = false,
    )

    @Serializable
    private data class RemoteQuietScheduleRow(
        @SerialName("user_id")
        val userId: String,
        @SerialName("is_enabled")
        val isEnabled: Boolean = true,
        @SerialName("start_hour")
        val startHour: Double = 22.5,
        @SerialName("end_hour")
        val endHour: Double = 6.0,
        @SerialName("active_days")
        val activeDays: List<Int> = UserSettingsDto.DEFAULT_REPEAT_DAYS,
        @SerialName("emergency_allowed")
        val emergencyAllowed: List<String> = emptyList(),
    )

    private companion object {
        const val COL_USER_ID = "user_id"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TABLE_USER_SETTINGS = "user_settings"
        const val TABLE_QUIET_SCHEDULES = "quiet_schedules"
        const val CHANNEL_GLOBAL_SYNC = "diu_global_sync"
        const val EVENT_PROFILE_UPDATED = "profile_updated"
        const val MINUTES_PER_HOUR = 60.0
        const val DECIMAL_SCALE = 10.0
        const val MAX_MINUTE_OF_DAY = 23 * 60 + 59
    }
}
