package com.uzuu.diuchat.feature.chat.domain.repository

import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface ChatRepository {
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>

    fun observeConversations(): Flow<List<Conversation>>

    fun observeConversation(conversationId: String): Flow<Conversation?>

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        mood: MoodType,
        replyToId: String? = null,
        replyQuote: String? = null,
    ): ChatMessage

    suspend fun sendPhotoMessage(
        conversationId: String,
        photoUri: String,
        caption: String?,
        mood: MoodType,
        replyToId: String? = null,
        replyQuote: String? = null,
    ): ChatMessage

    suspend fun sendVoiceMessage(
        conversationId: String,
        audioUri: String,
        durationMs: Long,
        waveform: List<Float>,
        mood: MoodType,
        replyToId: String? = null,
        replyQuote: String? = null,
    ): ChatMessage

    suspend fun createGroup(
        title: String,
        mood: MoodType,
        memberIds: List<String>,
    ): Conversation

    suspend fun updateGroupInfo(
        conversationId: String,
        title: String? = null,
        mood: MoodType? = null,
        nickname: String? = null,
        isMuted: Boolean? = null,
    )

    suspend fun manageGroupMember(
        conversationId: String,
        targetUserId: String,
        newRole: GroupRole? = null,
        isRemove: Boolean = false,
    )

    suspend fun leaveGroup(conversationId: String)

    suspend fun toggleReaction(messageId: String, mood: MoodType)

    suspend fun togglePinMessage(conversationId: String, messageId: String)

    suspend fun forwardMessage(
        message: ChatMessage,
        recipientConversationIds: List<String>,
    ): List<ChatMessage>

    suspend fun retryFailedMessage(messageId: String)

    suspend fun markAsRead(conversationId: String)

    suspend fun sendTypingStatus(conversationId: String, isTyping: Boolean) {
        // Default no-op for test doubles
    }

    fun observeTypingStatus(conversationId: String): Flow<Boolean> = emptyFlow()

    suspend fun deleteMessage(messageId: String) {
        // Default no-op for test doubles
    }

    suspend fun starMessage(messageId: String): Boolean = true

    suspend fun deleteConversation(conversationId: String) {
        // Default no-op for test doubles
    }

    suspend fun sendSealedMessage(
        conversationId: String,
        content: String,
        mood: MoodType,
        unlockAtEpochMs: Long,
        scheduledLabel: String,
    ): ChatMessage = sendMessage(
        conversationId = conversationId,
        content = content,
        mood = mood,
    )

    suspend fun loadOlderMessages(
        conversationId: String,
        beforeEpochMs: Long,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): Boolean = false

    companion object {
        const val DEFAULT_PAGE_SIZE = 30
    }
}
