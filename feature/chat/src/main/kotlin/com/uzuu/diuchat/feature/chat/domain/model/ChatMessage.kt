package com.uzuu.diuchat.feature.chat.domain.model

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val mood: MoodType,
    val status: MessageStatus,
    val replyToId: String? = null,
    val replyQuote: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true,
)
