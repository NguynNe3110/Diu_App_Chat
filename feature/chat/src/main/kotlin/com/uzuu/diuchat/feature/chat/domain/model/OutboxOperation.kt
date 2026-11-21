package com.uzuu.diuchat.feature.chat.domain.model

data class OutboxOperation(
    val id: String,
    val conversationId: String,
    val messageId: String,
    val status: String,
    val attemptCount: Int,
    val createdAtEpochMs: Long,
)
