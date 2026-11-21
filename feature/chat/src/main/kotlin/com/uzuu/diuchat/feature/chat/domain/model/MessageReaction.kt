package com.uzuu.diuchat.feature.chat.domain.model

data class MessageReaction(
    val messageId: String,
    val userId: String,
    val userName: String,
    val mood: MoodType,
    val isFromMe: Boolean = false,
)

data class ReactionSummary(
    val mood: MoodType,
    val count: Int,
    val includesMe: Boolean = false,
)
