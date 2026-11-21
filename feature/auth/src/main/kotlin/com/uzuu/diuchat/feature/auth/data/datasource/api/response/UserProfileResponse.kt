package com.uzuu.diuchat.feature.auth.data.datasource.api.response

import com.uzuu.diuchat.feature.auth.data.datasource.api.model.UserProfileDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    @SerialName("profile") val profile: UserProfileDto? = null,
)
