package com.uzuu.diuchat.feature.settings.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserProfileDto(
    @SerialName("id")
    val id: String,
    @SerialName("display_name")
    val displayName: String = "",
    @SerialName("username")
    val username: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("birthday")
    val birthday: String? = null,
    @SerialName("phone_number")
    val phone: String? = null,
    @SerialName("current_mood")
    val primaryMood: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
