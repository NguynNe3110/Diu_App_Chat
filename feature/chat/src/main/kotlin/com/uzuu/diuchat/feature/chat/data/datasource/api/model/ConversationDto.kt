package com.uzuu.diuchat.feature.chat.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String = "direct",
    @SerialName("title")
    val title: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("mood_id")
    val moodId: String = "thuong",
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)
