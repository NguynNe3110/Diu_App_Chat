package com.uzuu.diuchat.feature.media.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaVaultItemDto(
    @SerialName("id")
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("type")
    val type: String = "PHOTO",
    @SerialName("url")
    val url: String = "",
    @SerialName("caption")
    val caption: String = "",
    @SerialName("mood_name")
    val moodName: String = "Bình yên",
    @SerialName("mood_color_hex")
    val moodColorHex: String = "#A2D7AC",
    @SerialName("duration_seconds")
    val durationSeconds: Int = 0,
    @SerialName("timestamp")
    val timestamp: String = "",
    @SerialName("month_group")
    val monthGroup: String = "Tháng 11, 2026",
    @SerialName("sender_name")
    val senderName: String = "Bạn",
)
