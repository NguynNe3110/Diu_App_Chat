package com.uzuu.diuchat.feature.settings.data.datasource.api

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.response.UserSettingsResponse
import com.uzuu.diuchat.feature.settings.data.datasource.api.service.SettingsSupabaseService

internal class SettingRemoteDataSourceImpl(
    private val supabaseService: SettingsSupabaseService,
) : SettingRemoteDataSource {
    override suspend fun fetchUserProfile(): UserProfileDto? = supabaseService.fetchUserProfile()

    override suspend fun upsertUserProfile(profile: UserProfileDto) {
        supabaseService.upsertUserProfile(profile)
    }

    override suspend fun fetchUserSettings(): UserSettingsDto? = supabaseService.fetchUserSettings()

    override suspend fun upsertUserSettings(settings: UserSettingsDto) {
        supabaseService.upsertUserSettings(settings)
    }

    override suspend fun fetchUserBundle(): UserSettingsResponse = UserSettingsResponse(
        profile = supabaseService.fetchUserProfile(),
        settings = supabaseService.fetchUserSettings(),
    )

    override suspend fun signOut() {
        supabaseService.signOut()
    }
}
