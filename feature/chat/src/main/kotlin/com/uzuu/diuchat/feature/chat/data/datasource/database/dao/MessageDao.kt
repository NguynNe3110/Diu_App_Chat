package com.uzuu.diuchat.feature.chat.data.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.ConversationEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.MessageEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.OutboxOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAtEpochMs ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessage(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutbox(operation: OutboxOperationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: String,
    )

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTimeEpochMs = :lastMessageTimeEpochMs WHERE id = :id")
    suspend fun updateConversationLastMessage(
        id: String,
        lastMessage: String,
        lastMessageTimeEpochMs: Long,
    ): Int

    @Transaction
    suspend fun insertMessageWithOutbox(
        message: MessageEntity,
        outbox: OutboxOperationEntity,
    ) {
        insert(message)
        insertOutbox(outbox)
    }

    @Transaction
    suspend fun insertMessageWithOutboxAndSummary(
        message: MessageEntity,
        outbox: OutboxOperationEntity,
        conversationId: String,
        summaryContent: String,
        moodId: String,
        timestampEpochMs: Long,
    ) {
        insert(message)
        insertOutbox(outbox)
        val updated = updateConversationLastMessage(conversationId, summaryContent, timestampEpochMs)
        if (updated == 0) {
            upsertConversation(
                ConversationEntity(
                    id = conversationId,
                    title = "Cuộc hội thoại",
                    mood = moodId,
                    lastMessage = summaryContent,
                    lastMessageTimeEpochMs = timestampEpochMs,
                    unreadCount = 0,
                ),
            )
        }
    }

    @Transaction
    suspend fun retryMessageWithOutbox(
        messageId: String,
        status: String,
        outbox: OutboxOperationEntity,
    ) {
        updateStatus(messageId, status)
        insertOutbox(outbox)
    }
}
