package com.uzuu.diuchat.feature.chat.data.repository

import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.ConversationDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.MessageDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.OutboxDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.ConversationEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.MessageEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.OutboxOperationEntity
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val outboxDao: OutboxDao,
) : ChatRepository {
    override fun observeMessages(
        conversationId: String,
    ): Flow<List<ChatMessage>> = messageDao.observeMessages(conversationId).map { entities ->
        entities.map { it.toDomain() }
    }

    override fun observeConversations(): Flow<List<Conversation>> = conversationDao.observeConversations().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun sendMessage(
        conversationId: String,
        content: String,
        mood: MoodType,
        replyToId: String?,
        replyQuote: String?,
    ): ChatMessage {
        val messageId = UUID.randomUUID().toString()
        val operationId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1. Transactional Outbox: Write Pending message and outbox operation atomically
        val messageEntity =
            MessageEntity(
                id = messageId,
                conversationId = conversationId,
                senderId = "me",
                senderName = "Tôi",
                content = content,
                mood = mood.id,
                status = MessageStatus.PENDING.value,
                replyToId = replyToId,
                replyQuote = replyQuote,
                createdAtEpochMs = now,
                isFromMe = true,
                mediaType = MessageMediaType.TEXT.name,
            )

        val outboxEntity =
            OutboxOperationEntity(
                id = operationId,
                conversationId = conversationId,
                messageId = messageId,
                status = MessageStatus.PENDING.value,
                attemptCount = 0,
                createdAtEpochMs = now,
            )

        messageDao.insert(messageEntity)
        outboxDao.insert(outboxEntity)

        updateConversationSummary(conversationId, content, mood, now)

        // 2. Optimistic drain / sync: Transition status to SENT
        messageDao.updateStatus(messageId, MessageStatus.SENT.value)
        outboxDao.updateStatus(operationId, MessageStatus.SENT.value)

        return messageEntity.copy(status = MessageStatus.SENT.value).toDomain()
    }

    override suspend fun sendPhotoMessage(
        conversationId: String,
        photoUri: String,
        caption: String?,
        mood: MoodType,
        replyToId: String?,
        replyQuote: String?,
    ): ChatMessage {
        val messageId = UUID.randomUUID().toString()
        val operationId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val displayContent = caption?.takeIf { it.isNotBlank() } ?: "📷 [Hình ảnh]"

        val messageEntity =
            MessageEntity(
                id = messageId,
                conversationId = conversationId,
                senderId = "me",
                senderName = "Tôi",
                content = displayContent,
                mood = mood.id,
                status = MessageStatus.PENDING.value,
                replyToId = replyToId,
                replyQuote = replyQuote,
                createdAtEpochMs = now,
                isFromMe = true,
                mediaType = MessageMediaType.PHOTO.name,
                mediaUri = photoUri,
                caption = caption,
            )

        val outboxEntity =
            OutboxOperationEntity(
                id = operationId,
                conversationId = conversationId,
                messageId = messageId,
                status = MessageStatus.PENDING.value,
                attemptCount = 0,
                createdAtEpochMs = now,
            )

        messageDao.insert(messageEntity)
        outboxDao.insert(outboxEntity)

        updateConversationSummary(conversationId, displayContent, mood, now)

        messageDao.updateStatus(messageId, MessageStatus.SENT.value)
        outboxDao.updateStatus(operationId, MessageStatus.SENT.value)

        return messageEntity.copy(status = MessageStatus.SENT.value).toDomain()
    }

    override suspend fun sendVoiceMessage(
        conversationId: String,
        audioUri: String,
        durationMs: Long,
        waveform: List<Float>,
        mood: MoodType,
        replyToId: String?,
        replyQuote: String?,
    ): ChatMessage {
        val messageId = UUID.randomUUID().toString()
        val operationId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val summary = "🎤 [Tin nhắn thoại]"

        val messageEntity =
            MessageEntity(
                id = messageId,
                conversationId = conversationId,
                senderId = "me",
                senderName = "Tôi",
                content = summary,
                mood = mood.id,
                status = MessageStatus.PENDING.value,
                replyToId = replyToId,
                replyQuote = replyQuote,
                createdAtEpochMs = now,
                isFromMe = true,
                mediaType = MessageMediaType.VOICE.name,
                mediaUri = audioUri,
                audioDurationMs = durationMs,
                audioWaveformCsv = waveform.joinToString(","),
            )

        val outboxEntity =
            OutboxOperationEntity(
                id = operationId,
                conversationId = conversationId,
                messageId = messageId,
                status = MessageStatus.PENDING.value,
                attemptCount = 0,
                createdAtEpochMs = now,
            )

        messageDao.insert(messageEntity)
        outboxDao.insert(outboxEntity)

        updateConversationSummary(conversationId, summary, mood, now)

        messageDao.updateStatus(messageId, MessageStatus.SENT.value)
        outboxDao.updateStatus(operationId, MessageStatus.SENT.value)

        return messageEntity.copy(status = MessageStatus.SENT.value).toDomain()
    }

    override suspend fun retryFailedMessage(messageId: String) {
        val message = messageDao.getMessage(messageId) ?: return
        messageDao.updateStatus(messageId, MessageStatus.PENDING.value)
        val opId = UUID.randomUUID().toString()
        outboxDao.insert(
            OutboxOperationEntity(
                id = opId,
                conversationId = message.conversationId,
                messageId = messageId,
                status = MessageStatus.PENDING.value,
            ),
        )
        // Transition to SENT
        messageDao.updateStatus(messageId, MessageStatus.SENT.value)
        outboxDao.updateStatus(opId, MessageStatus.SENT.value)
    }

    override suspend fun markAsRead(conversationId: String) {
        val convo = conversationDao.getConversation(conversationId)
        if (convo != null && convo.unreadCount > 0) {
            conversationDao.upsert(convo.copy(unreadCount = 0))
        }
    }

    private suspend fun updateConversationSummary(
        conversationId: String,
        lastMessage: String,
        mood: MoodType,
        now: Long,
    ) {
        val existingConversation = conversationDao.getConversation(conversationId)
        if (existingConversation != null) {
            conversationDao.updateLastMessage(conversationId, lastMessage, now)
        } else {
            conversationDao.upsert(
                ConversationEntity(
                    id = conversationId,
                    title = "Cuộc hội thoại",
                    mood = mood.id,
                    lastMessage = lastMessage,
                    lastMessageTimeEpochMs = now,
                    unreadCount = 0,
                ),
            )
        }
    }

    private fun MessageEntity.toDomain(): ChatMessage = ChatMessage(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        senderName = senderName,
        content = content,
        mood = MoodType.fromId(mood),
        status = MessageStatus.fromValue(status),
        replyToId = replyToId,
        replyQuote = replyQuote,
        createdAtEpochMs = createdAtEpochMs,
        isFromMe = isFromMe,
        mediaType = runCatching { MessageMediaType.valueOf(mediaType) }.getOrDefault(MessageMediaType.TEXT),
        mediaUri = mediaUri,
        audioDurationMs = audioDurationMs,
        audioWaveform = audioWaveformCsv?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: emptyList(),
        caption = caption,
    )

    private fun ConversationEntity.toDomain(): Conversation = Conversation(
        id = id,
        title = title,
        mood = MoodType.fromId(mood),
        lastMessage = lastMessage,
        lastMessageTimeEpochMs = lastMessageTimeEpochMs,
        unreadCount = unreadCount,
    )
}
