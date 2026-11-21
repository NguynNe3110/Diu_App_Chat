package com.uzuu.diuchat.feature.chat.data.datasource.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.ConversationDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.MessageDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.OutboxDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.ConversationEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.MessageEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.OutboxOperationEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        OutboxOperationEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class DiuChatDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun outboxDao(): OutboxDao
}
