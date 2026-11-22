package com.uzuu.diuchat.feature.chat.data.datasource.api.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ConversationDto(
    @SerialName("id")
    val id: String,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("type")
    val type: String = "direct",
    @SerialName("title")
    val title: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("mood_id")
    val moodId: String = "thuong",
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)
