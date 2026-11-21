package com.uzuu.diuchat.feature.chat.domain.repository

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>

    fun observeConversations(): Flow<List<Conversation>>

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        mood: MoodType,
        replyToId: String? = null,
        replyQuote: String? = null,
    ): ChatMessage

    suspend fun retryFailedMessage(messageId: String)

    suspend fun markAsRead(conversationId: String)
}
