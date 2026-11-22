package com.uzuu.diuchat.feature.chat.data.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageTimeEpochMs DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun observeConversation(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversation(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTimeEpochMs = :lastMessageTimeEpochMs WHERE id = :id")
    suspend fun updateLastMessage(
        id: String,
        lastMessage: String,
        lastMessageTimeEpochMs: Long,
    )

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)
}
