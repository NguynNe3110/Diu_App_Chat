package com.uzuu.diuchat.feature.chat.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.uzuu.diuchat.feature.base.data.media.ImageDownscaler
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.service.ChatSupabaseService
import com.uzuu.diuchat.feature.chat.data.datasource.database.DiuChatDatabase
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.ConversationDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.MessageDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.OutboxDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.ConversationEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.MessageEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.OutboxOperationEntity
import com.uzuu.diuchat.feature.chat.data.mapper.ChatMapper
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.GroupError
import com.uzuu.diuchat.feature.chat.domain.model.GroupMember
import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageReaction
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Suppress("TooManyFunctions")
class ChatRepositoryImpl(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val outboxDao: OutboxDao,
    private val database: DiuChatDatabase? = null,
    private val chatMapper: ChatMapper = ChatMapper(),
    private val supabaseService: ChatSupabaseService = ChatSupabaseService(null),
    private val context: Context? = null,
    private val imageDownscaler: ImageDownscaler? = null,
) : ChatRepository {
    private val messageWindowLimits = ConcurrentHashMap<String, MutableStateFlow<Int>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMessages(
        conversationId: String,
    ): Flow<List<ChatMessage>> = channelFlow {
        val windowLimitFlow =
            messageWindowLimits.compute(conversationId) { _, existing ->
                existing?.apply { value = ChatRepository.DEFAULT_PAGE_SIZE }
                    ?: MutableStateFlow(ChatRepository.DEFAULT_PAGE_SIZE)
            }!!
        launch {
            syncInitialMessages(conversationId)
        }
        launch {
            supabaseService.observeRealtimeMessages(conversationId).collect { dto ->
                upsertIncomingMessage(dto)
            }
        }
        launch {
            windowLimitFlow
                .flatMapLatest { limit -> messageDao.observeLatestMessages(conversationId, limit) }
                .collect { entities ->
                    send(entities.map(chatMapper::messageEntityToDomain))
                }
        }
    }

    override suspend fun loadOlderMessages(
        conversationId: String,
        beforeEpochMs: Long,
        limit: Int,
    ): Boolean {
        val safeLimit = limit.coerceAtLeast(1)
        val beforeIso = Instant.ofEpochMilli(beforeEpochMs).toString()
        val response =
            supabaseService
                .fetchMessages(
                    conversationId = conversationId,
                    limit = safeLimit,
                    beforeCreatedAt = beforeIso,
                ).getOrNull()
        val activeUserId = currentUserId()
        val fetchedEntities =
            response?.messages.orEmpty().map { dto ->
                chatMapper.messageDtoToEntity(dto, activeUserId)
            }
        if (fetchedEntities.isNotEmpty()) {
            messageDao.insertAll(fetchedEntities)
        }
        val windowLimitFlow =
            messageWindowLimits.computeIfAbsent(conversationId) {
                MutableStateFlow(ChatRepository.DEFAULT_PAGE_SIZE)
            }
        windowLimitFlow.update { current -> current + safeLimit }

        val oldestEpochAfterFetch =
            fetchedEntities.minOfOrNull { it.createdAtEpochMs } ?: beforeEpochMs
        val hasMoreLocal = messageDao.countOlderMessages(conversationId, oldestEpochAfterFetch) > 0
        return (response?.hasMore == true) || hasMoreLocal
    }

    override fun observeConversations(): Flow<List<Conversation>> = channelFlow {
        launch {
            supabaseService.fetchConversations().getOrNull()?.forEach { dto ->
                conversationDao.upsert(chatMapper.conversationDtoToEntity(dto))
            }
        }
        launch {
            conversationDao.observeConversations().collect { entities ->
                send(entities.map(chatMapper::conversationEntityToDomain))
            }
        }
    }

    override fun observeConversation(
        conversationId: String,
    ): Flow<Conversation?> = channelFlow {
        launch {
            supabaseService.observeProfileUpdates().collect {
                supabaseService.fetchConversations().getOrNull()?.firstOrNull { it.id == conversationId }?.let { dto ->
                    conversationDao.upsert(chatMapper.conversationDtoToEntity(dto))
                }
            }
        }
        launch {
            conversationDao.observeConversation(conversationId).collect { entity ->
                send(entity?.let(chatMapper::conversationEntityToDomain))
            }
        }
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

        val messageEntity =
            MessageEntity(
                id = messageId,
                conversationId = conversationId,
                senderId = currentUserId(),
                senderName = currentUserName(),
                content = content,
                mood = mood.id,
                status = MessageStatus.PENDING.value,
                replyToId = replyToId,
                replyQuote = replyQuote,
                createdAtEpochMs = now,
                isFromMe = true,
                mediaType = MessageMediaType.TEXT.name,
            )

        val outboxEntity = createOutboxEntity(operationId, conversationId, messageId, now)
        persistMessageWithOutbox(messageEntity, outboxEntity, mood)
        val syncedEntity = syncMessageWithOutboxAck(messageEntity, operationId)

        return chatMapper.messageEntityToDomain(syncedEntity)
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
        val displayContent = caption?.takeIf { it.isNotBlank() } ?: PHOTO_PLACEHOLDER

        val messageEntity =
            MessageEntity(
                id = messageId,
                conversationId = conversationId,
                senderId = currentUserId(),
                senderName = currentUserName(),
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

        val outboxEntity = createOutboxEntity(operationId, conversationId, messageId, now)
        persistMessageWithOutbox(messageEntity, outboxEntity, mood)

        val uploadedUrl = uploadLocalPhotoIfNeeded(conversationId, messageId, photoUri)
        val entityForSync =
            if (!uploadedUrl.isNullOrBlank()) {
                val updated = messageEntity.copy(mediaUri = uploadedUrl)
                messageDao.insert(updated)
                updated
            } else {
                messageEntity
            }
        val syncedEntity = syncMessageWithOutboxAck(entityForSync, operationId)

        return chatMapper.messageEntityToDomain(syncedEntity)
    }

    private suspend fun uploadLocalPhotoIfNeeded(
        conversationId: String,
        messageId: String,
        photoUri: String,
    ): String? {
        val isLocalUri = photoUri.startsWith("content://") || photoUri.startsWith("file://")
        val bytes =
            if (isLocalUri) {
                imageDownscaler?.downscale(photoUri)
                    ?: runCatching {
                        context?.contentResolver?.openInputStream(Uri.parse(photoUri))?.use { it.readBytes() }
                    }.getOrNull()
            } else {
                null
            }
        return bytes?.let { data ->
            supabaseService
                .uploadMedia(bucketPath = "$conversationId/$messageId.jpg", bytes = data)
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
        }
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

        val messageEntity =
            MessageEntity(
                id = messageId,
                conversationId = conversationId,
                senderId = currentUserId(),
                senderName = currentUserName(),
                content = VOICE_PLACEHOLDER,
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

        val outboxEntity = createOutboxEntity(operationId, conversationId, messageId, now)
        persistMessageWithOutbox(messageEntity, outboxEntity, mood)
        val syncedEntity = syncMessageWithOutboxAck(messageEntity, operationId)

        return chatMapper.messageEntityToDomain(syncedEntity)
    }

    override suspend fun createGroup(
        title: String,
        mood: MoodType,
        memberIds: List<String>,
    ): Conversation {
        val groupId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val membersList = mutableListOf(
            GroupMember(
                userId = currentUserId(),
                displayName = currentUserName(),
                role = GroupRole.ADMIN,
                mood = mood,
                isFromMe = true,
            ),
        )
        memberIds.forEach { id ->
            membersList.add(
                GroupMember(
                    userId = id,
                    displayName = "Thành viên $id",
                    role = GroupRole.MEMBER,
                    mood = MoodType.BINHYEN,
                ),
            )
        }

        val entity = ConversationEntity(
            id = groupId,
            title = title,
            mood = mood.id,
            lastMessage = "Nhóm vừa được tạo",
            lastMessageTimeEpochMs = now,
            unreadCount = 0,
            isGroup = true,
            daysTogether = 1,
            membersCsv = chatMapper.serializeMembers(membersList),
        )

        conversationDao.upsert(entity)
        val creatorId = currentUserId()
        supabaseService.upsertConversation(
            chatMapper.conversationEntityToDto(entity).copy(createdBy = creatorId),
        )
        supabaseService.insertConversationMembers(
            conversationId = groupId,
            memberIds = memberIds,
            creatorId = creatorId,
        )
        return chatMapper.conversationEntityToDomain(entity)
    }

    override suspend fun updateGroupInfo(
        conversationId: String,
        title: String?,
        mood: MoodType?,
        nickname: String?,
        isMuted: Boolean?,
    ) {
        val existing = conversationDao.getConversation(conversationId) ?: return
        val updated = existing.copy(
            title = title ?: existing.title,
            mood = mood?.id ?: existing.mood,
            customNickname = nickname ?: existing.customNickname,
            isMuted = isMuted ?: existing.isMuted,
        )
        conversationDao.upsert(updated)
        supabaseService.upsertConversation(chatMapper.conversationEntityToDto(updated))
    }

    override suspend fun manageGroupMember(
        conversationId: String,
        targetUserId: String,
        newRole: GroupRole?,
        isRemove: Boolean,
    ) {
        val existing = conversationDao.getConversation(conversationId) ?: return
        val members = chatMapper.parseMembers(existing.membersCsv).toMutableList()

        if (isRemove) {
            members.removeAll { it.userId == targetUserId }
        } else if (newRole != null) {
            val idx = members.indexOfFirst { it.userId == targetUserId }
            if (idx >= 0) {
                members[idx] = members[idx].copy(role = newRole)
            }
        }

        val updated = existing.copy(membersCsv = chatMapper.serializeMembers(members))
        conversationDao.upsert(updated)
        supabaseService.upsertConversation(chatMapper.conversationEntityToDto(updated))
    }

    override suspend fun leaveGroup(conversationId: String) {
        val existing = conversationDao.getConversation(conversationId) ?: return
        val members = chatMapper.parseMembers(existing.membersCsv)
        val userId = currentUserId()

        val callerMember = members.find { it.isFromMe || it.userId == userId }
        val otherAdmins = members.filter { (it.isFromMe || it.userId == userId).not() && it.role == GroupRole.ADMIN }

        if (callerMember?.role == GroupRole.ADMIN && otherAdmins.isEmpty() && members.size > 1) {
            throw GroupError.LastAdminCannotLeave
        }

        val remainingMembers = members.filterNot { it.isFromMe || it.userId == userId }
        val updated = existing.copy(membersCsv = chatMapper.serializeMembers(remainingMembers))
        conversationDao.upsert(updated)
        supabaseService.upsertConversation(chatMapper.conversationEntityToDto(updated))
    }

    override suspend fun toggleReaction(
        messageId: String,
        mood: MoodType,
    ) {
        val message = messageDao.getMessage(messageId) ?: return
        val reactions = chatMapper.parseReactions(message.reactionsCsv, messageId).toMutableList()
        val userId = currentUserId()
        val userName = currentUserName()

        val myExistingIndex = reactions.indexOfFirst { it.userId == userId }
        if (myExistingIndex >= 0) {
            val existingReaction = reactions[myExistingIndex]
            if (existingReaction.mood == mood) {
                reactions.removeAt(myExistingIndex)
            } else {
                reactions[myExistingIndex] = existingReaction.copy(mood = mood)
            }
        } else {
            reactions.add(
                MessageReaction(
                    messageId = messageId,
                    userId = userId,
                    userName = userName,
                    mood = mood,
                    isFromMe = true,
                ),
            )
        }

        val updated = message.copy(reactionsCsv = chatMapper.serializeReactions(reactions))
        messageDao.insert(updated)
        supabaseService.upsertMessage(chatMapper.messageEntityToDto(updated))
    }

    override suspend fun togglePinMessage(
        conversationId: String,
        messageId: String,
    ) {
        val convo = conversationDao.getConversation(conversationId) ?: return
        val message = messageDao.getMessage(messageId) ?: return

        val (updatedConvo, updatedMessage) =
            if (convo.pinnedMessageId == messageId) {
                convo.copy(pinnedMessageId = null, pinnedMessageSnippet = null) to
                    message.copy(isPinned = false)
            } else {
                val previousPinnedId = convo.pinnedMessageId
                if (previousPinnedId != null && previousPinnedId != messageId) {
                    messageDao.getMessage(previousPinnedId)?.let { prevMsg ->
                        val unpinnedPrev = prevMsg.copy(isPinned = false)
                        messageDao.insert(unpinnedPrev)
                        supabaseService.upsertMessage(chatMapper.messageEntityToDto(unpinnedPrev))
                    }
                }
                convo.copy(
                    pinnedMessageId = messageId,
                    pinnedMessageSnippet = message.content.take(PIN_SNIPPET_LENGTH),
                ) to message.copy(isPinned = true)
            }

        conversationDao.upsert(updatedConvo)
        messageDao.insert(updatedMessage)
        supabaseService.upsertConversation(chatMapper.conversationEntityToDto(updatedConvo))
        supabaseService.upsertMessage(chatMapper.messageEntityToDto(updatedMessage))
    }

    override suspend fun forwardMessage(
        message: ChatMessage,
        recipientConversationIds: List<String>,
    ): List<ChatMessage> {
        val forwardedList = mutableListOf<ChatMessage>()
        val now = System.currentTimeMillis()
        val userId = currentUserId()
        val userName = currentUserName()

        recipientConversationIds.forEach { targetConvoId ->
            val newMsgId = UUID.randomUUID().toString()
            val opId = UUID.randomUUID().toString()

            val entity = MessageEntity(
                id = newMsgId,
                conversationId = targetConvoId,
                senderId = userId,
                senderName = userName,
                content = message.content,
                mood = message.mood.id,
                status = MessageStatus.PENDING.value,
                mediaType = message.mediaType.name,
                mediaUri = message.mediaUri,
                audioDurationMs = message.audioDurationMs,
                audioWaveformCsv = message.audioWaveform.joinToString(","),
                caption = message.caption,
                createdAtEpochMs = now,
                isFromMe = true,
            )

            val outbox = createOutboxEntity(opId, targetConvoId, newMsgId, now)
            persistMessageWithOutbox(entity, outbox, message.mood)
            val syncedEntity = syncMessageWithOutboxAck(entity, opId)
            forwardedList.add(chatMapper.messageEntityToDomain(syncedEntity))
        }

        return forwardedList
    }

    override suspend fun retryFailedMessage(messageId: String) {
        val message = messageDao.getMessage(messageId) ?: return
        val opId = UUID.randomUUID().toString()
        val outbox = createOutboxEntity(opId, message.conversationId, messageId, System.currentTimeMillis())

        suspend fun persistRetry() {
            messageDao.retryMessageWithOutbox(messageId, MessageStatus.PENDING.value, outbox)
        }

        database?.withTransaction {
            persistRetry()
        } ?: persistRetry()

        syncMessageWithOutboxAck(message.copy(status = MessageStatus.PENDING.value), opId)
    }

    override suspend fun markAsRead(conversationId: String) {
        val convo = conversationDao.getConversation(conversationId)
        if (convo != null && convo.unreadCount > 0) {
            val updated = convo.copy(unreadCount = 0)
            conversationDao.upsert(updated)
            supabaseService.upsertConversation(chatMapper.conversationEntityToDto(updated))
        }
        supabaseService.markConversationRead(conversationId)
    }

    override suspend fun sendTypingStatus(
        conversationId: String,
        isTyping: Boolean,
    ) {
        supabaseService.broadcastTyping(conversationId, isTyping)
    }

    override fun observeTypingStatus(conversationId: String): Flow<Boolean> = supabaseService.observeTypingStatus(conversationId)

    override suspend fun deleteMessage(messageId: String) {
        messageDao.delete(messageId)
        supabaseService.deleteMessage(messageId)
    }

    override suspend fun starMessage(messageId: String): Boolean = supabaseService.toggleStarMessage(messageId).getOrDefault(true)

    override suspend fun deleteConversation(conversationId: String) {
        messageDao.deleteByConversationId(conversationId)
        conversationDao.deleteById(conversationId)
        supabaseService.deleteConversation(conversationId)
    }

    override suspend fun sendSealedMessage(
        conversationId: String,
        content: String,
        mood: MoodType,
        unlockAtEpochMs: Long,
        scheduledLabel: String,
    ): ChatMessage {
        val messageId = UUID.randomUUID().toString()
        val operationId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity =
            MessageEntity(
                id = messageId,
                conversationId = conversationId,
                senderId = currentUserId(),
                senderName = currentUserName(),
                content = content,
                mood = mood.id,
                status = MessageStatus.PENDING.value,
                createdAtEpochMs = now,
                isFromMe = true,
                mediaType = MessageMediaType.SEALED_LETTER.name,
                audioDurationMs = unlockAtEpochMs,
                caption = scheduledLabel,
            )
        val outbox = createOutboxEntity(operationId, conversationId, messageId, now)
        persistMessageWithOutbox(entity, outbox, mood)
        val syncedEntity = syncMessageWithOutboxAck(entity, operationId)
        supabaseService.insertSealedLetterForMessage(
            messageId = messageId,
            conversationId = conversationId,
            content = content,
            unlockAtEpochMs = unlockAtEpochMs,
        )
        return chatMapper.messageEntityToDomain(syncedEntity).copy(
            scheduledForLabel = scheduledLabel,
            unlockAtEpochMs = unlockAtEpochMs,
            isSealedRevealed = false,
        )
    }

    private fun currentUserId(): String = supabaseService.currentUserId() ?: ME_USER_ID

    private fun currentUserName(): String = supabaseService.currentUserDisplayName()?.takeIf { it.isNotBlank() } ?: ME_DISPLAY_NAME

    private suspend fun syncInitialMessages(conversationId: String) {
        val response =
            supabaseService
                .fetchMessages(
                    conversationId = conversationId,
                    limit = ChatRepository.DEFAULT_PAGE_SIZE,
                ).getOrNull() ?: return
        response.conversation?.let { convoDto ->
            conversationDao.upsert(chatMapper.conversationDtoToEntity(convoDto))
        }
        val activeUserId = currentUserId()
        response.messages.forEach { dto ->
            messageDao.insert(chatMapper.messageDtoToEntity(dto, activeUserId))
        }
        supabaseService.markConversationRead(conversationId)
    }

    private suspend fun upsertIncomingMessage(dto: MessageDto) {
        val activeUserId = currentUserId()
        val entity = chatMapper.messageDtoToEntity(dto, activeUserId)
        messageDao.insert(entity)
        updateConversationSummary(
            conversationId = entity.conversationId,
            lastMessage = entity.content,
            mood = MoodType.fromId(entity.mood),
            now = entity.createdAtEpochMs,
        )
        if (!entity.isFromMe) {
            supabaseService.markConversationRead(entity.conversationId)
        }
    }

    private suspend fun syncMessageWithOutboxAck(
        messageEntity: MessageEntity,
        operationId: String,
    ): MessageEntity {
        val syncResult =
            supabaseService.upsertMessage(
                chatMapper.messageEntityToDto(messageEntity.copy(status = MessageStatus.SENT.value)),
            )
        return if (syncResult.isSuccess) {
            messageDao.updateStatus(messageEntity.id, MessageStatus.SENT.value)
            outboxDao.delete(operationId)
            messageEntity.copy(status = MessageStatus.SENT.value)
        } else {
            messageDao.updateStatus(messageEntity.id, MessageStatus.FAILED.value)
            outboxDao.updateStatus(operationId, MessageStatus.FAILED.value)
            messageEntity.copy(status = MessageStatus.FAILED.value)
        }
    }

    private fun createOutboxEntity(
        operationId: String,
        conversationId: String,
        messageId: String,
        createdAtEpochMs: Long,
    ): OutboxOperationEntity = OutboxOperationEntity(
        id = operationId,
        conversationId = conversationId,
        messageId = messageId,
        status = MessageStatus.PENDING.value,
        attemptCount = 0,
        createdAtEpochMs = createdAtEpochMs,
    )

    private suspend fun persistMessageWithOutbox(
        messageEntity: MessageEntity,
        outboxEntity: OutboxOperationEntity,
        mood: MoodType,
    ) {
        suspend fun persist() {
            val summaryText = if (messageEntity.mediaType == MessageMediaType.SEALED_LETTER.name) {
                "✉️ Thư hẹn · mở vào ${messageEntity.caption ?: "đúng giờ"}"
            } else {
                messageEntity.content
            }
            messageDao.insertMessageWithOutbox(messageEntity, outboxEntity)
            updateConversationSummary(
                conversationId = messageEntity.conversationId,
                lastMessage = summaryText,
                mood = mood,
                now = messageEntity.createdAtEpochMs,
            )
        }

        database?.withTransaction {
            persist()
        } ?: persist()
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

    private companion object {
        const val ME_USER_ID = "me"
        const val ME_DISPLAY_NAME = "Tôi"
        const val PHOTO_PLACEHOLDER = "📷 [Hình ảnh]"
        const val VOICE_PLACEHOLDER = "🎤 [Tin nhắn thoại]"
        const val PIN_SNIPPET_LENGTH = 60
    }
}
