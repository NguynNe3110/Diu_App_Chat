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
    val username: String = "",
    @SerialName("bio")
    val bio: String = "",
    @SerialName("birthday")
    val birthday: String = "",
    @SerialName("phone")
    val phone: String = "",
    @SerialName("primary_mood")
    val primaryMood: String = "Bình yên",
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
