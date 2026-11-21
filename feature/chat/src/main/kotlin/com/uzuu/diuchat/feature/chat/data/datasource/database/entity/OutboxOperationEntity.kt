package com.uzuu.diuchat.feature.chat.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox_operations")
data class OutboxOperationEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val messageId: String,
    val status: String,
    val attemptCount: Int = 0,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
)
