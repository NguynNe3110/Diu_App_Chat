package com.uzuu.diuchat.feature.settings.data.datasource.api.service

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

internal class SettingsSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    fun currentUserId(): String? = supabaseClient?.auth?.currentUserOrNull()?.id

    suspend fun fetchUserProfile(): UserProfileDto? {
        val client = supabaseClient ?: return null
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return client.postgrest
            .from(TABLE_USER_PROFILES)
            .select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<UserProfileDto>()
    }

    suspend fun upsertUserProfile(profile: UserProfileDto) {
        val client = supabaseClient ?: return
        client.postgrest
            .from(TABLE_USER_PROFILES)
            .upsert(profile)
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
        val client = supabaseClient ?: return
        client.auth.signOut()
    }

    private companion object {
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TABLE_USER_SETTINGS = "user_settings"
    }
}
