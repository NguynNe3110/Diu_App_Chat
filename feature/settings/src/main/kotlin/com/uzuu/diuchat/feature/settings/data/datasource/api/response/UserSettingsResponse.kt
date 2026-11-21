package com.uzuu.diuchat.feature.settings.data.datasource.api.response

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserSettingsResponse(
    @SerialName("profile")
    val profile: UserProfileDto? = null,
    @SerialName("settings")
    val settings: UserSettingsDto? = null,
)
