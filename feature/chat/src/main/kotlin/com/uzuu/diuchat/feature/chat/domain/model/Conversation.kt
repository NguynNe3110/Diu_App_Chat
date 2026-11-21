package com.uzuu.diuchat.feature.chat.domain.model

data class Conversation(
    val id: String,
    val title: String,
    val mood: MoodType,
    val lastMessage: String? = null,
    val lastMessageTimeEpochMs: Long? = null,
    val unreadCount: Int = 0,
)
