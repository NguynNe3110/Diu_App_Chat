package com.uzuu.diuchat.feature.chat.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val mood: String,
    val status: String,
    val replyToId: String? = null,
    val replyQuote: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true,
)
