package com.uzuu.diuchat.feature.chat.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val mood: String,
    val lastMessage: String? = null,
    val lastMessageTimeEpochMs: Long? = null,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val avatarUrl: String? = null,
    val pinnedMessageId: String? = null,
    val pinnedMessageSnippet: String? = null,
    val daysTogether: Int? = null,
    val anniversaryDate: String? = null,
    val customNickname: String? = null,
    val isMuted: Boolean = false,
    val membersCsv: String? = null,
)
