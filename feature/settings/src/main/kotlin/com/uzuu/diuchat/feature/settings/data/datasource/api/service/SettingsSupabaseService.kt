package com.uzuu.diuchat.feature.settings.data.datasource.api.service

import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

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
                    put("user_id", profile.id)
                },
            )
        }
    }

    suspend fun fetchUserSettings(): UserSettingsDto? {
        val client = supabaseClient ?: return null
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return client.postgrest
            .from(TABLE_USER_SETTINGS)
            .select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<UserSettingsDto>()
    }

    suspend fun upsertUserSettings(settings: UserSettingsDto) {
        val client = supabaseClient ?: return
        client.postgrest
            .from(TABLE_USER_SETTINGS)
            .upsert(settings)
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

    private companion object {
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TABLE_USER_SETTINGS = "user_settings"
        const val CHANNEL_GLOBAL_SYNC = "diu_global_sync"
        const val EVENT_PROFILE_UPDATED = "profile_updated"
    }
}
