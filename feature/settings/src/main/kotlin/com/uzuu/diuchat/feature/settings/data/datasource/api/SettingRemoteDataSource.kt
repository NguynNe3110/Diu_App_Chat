package com.uzuu.diuchat.feature.settings.data.datasource.api

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.response.UserSettingsResponse

internal interface SettingRemoteDataSource {
    suspend fun fetchUserProfile(): UserProfileDto?
    suspend fun upsertUserProfile(profile: UserProfileDto)
    suspend fun fetchUserSettings(): UserSettingsDto?
    suspend fun upsertUserSettings(settings: UserSettingsDto)
    suspend fun fetchUserBundle(): UserSettingsResponse
    suspend fun signOut()
}
