package com.uzuu.diuchat.feature.home.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecentChatDto(
    @SerialName("id")
    val id: String,
    @SerialName("contact_name")
    val contactName: String,
    @SerialName("initials")
    val initials: String,
    @SerialName("mood_color_hex")
    val moodColorHex: String = "#F7A3A9",
    @SerialName("last_message")
    val lastMessage: String = "",
    @SerialName("timestamp")
    val timestamp: String = "",
    @SerialName("unread_count")
    val unreadCount: Int = 0,
    @SerialName("is_pending_letter")
    val isPendingLetter: Boolean = false,
    @SerialName("is_quiet")
    val isQuiet: Boolean = false,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
