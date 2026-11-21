package com.uzuu.diuchat.feature.chat.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("mood")
    val mood: String = "binh_yen",
    @SerialName("last_message")
    val lastMessage: String? = null,
    @SerialName("last_message_time_epoch_ms")
    val lastMessageTimeEpochMs: Long? = null,
    @SerialName("unread_count")
    val unreadCount: Int = 0,
    @SerialName("is_group")
    val isGroup: Boolean = false,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("pinned_message_id")
    val pinnedMessageId: String? = null,
    @SerialName("pinned_message_snippet")
    val pinnedMessageSnippet: String? = null,
    @SerialName("days_together")
    val daysTogether: Int? = null,
    @SerialName("anniversary_date")
    val anniversaryDate: String? = null,
    @SerialName("custom_nickname")
    val customNickname: String? = null,
    @SerialName("is_muted")
    val isMuted: Boolean = false,
    @SerialName("members_csv")
    val membersCsv: String? = null,
)
