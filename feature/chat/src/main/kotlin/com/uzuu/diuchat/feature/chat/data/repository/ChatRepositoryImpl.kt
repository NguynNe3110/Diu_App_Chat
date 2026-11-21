package com.uzuu.diuchat.feature.chat.data.repository

import androidx.room.withTransaction
import com.uzuu.diuchat.feature.chat.data.datasource.database.DiuChatDatabase
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.ConversationDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.MessageDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.OutboxDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.ConversationEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.MessageEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.OutboxOperationEntity
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
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val outboxDao: OutboxDao,
    private val database: DiuChatDatabase? = null,
) : ChatRepository {
    override fun observeMessages(
        conversationId: String,
    ): Flow<List<ChatMessage>> = messageDao.observeMessages(conversationId).map { entities ->
        entities.map { it.toDomain() }
    }

    override fun observeConversations(): Flow<List<Conversation>> = conversationDao.observeConversations().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun observeConversation(
        conversationId: String,
    ): Flow<Conversation?> = conversationDao.observeConversation(conversationId).map { it?.toDomain() }

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

        suspend fun persistLocalMessage() {
            messageDao.insert(messageEntity)
            outboxDao.insert(outboxEntity)
            updateConversationSummary(conversationId, content, mood, now)
        }

        database?.withTransaction {
            persistLocalMessage()
        } ?: persistLocalMessage()

        return messageEntity.toDomain()
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

        suspend fun persistLocalPhoto() {
            messageDao.insert(messageEntity)
            outboxDao.insert(outboxEntity)
            updateConversationSummary(conversationId, displayContent, mood, now)
        }

        database?.withTransaction {
            persistLocalPhoto()
        } ?: persistLocalPhoto()

        return messageEntity.toDomain()
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

        suspend fun persistLocalVoice() {
            messageDao.insert(messageEntity)
            outboxDao.insert(outboxEntity)
            updateConversationSummary(conversationId, summary, mood, now)
        }

        database?.withTransaction {
            persistLocalVoice()
        } ?: persistLocalVoice()

        return messageEntity.toDomain()
    }

    override suspend fun createGroup(
        title: String,
        mood: MoodType,
        memberIds: List<String>,
    ): Conversation {
        val groupId = "group_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()

        val membersList = mutableListOf(
            GroupMember(userId = "me", displayName = "Tôi", role = GroupRole.ADMIN, mood = mood, isFromMe = true),
        )
        memberIds.forEach { id ->
            membersList.add(
                GroupMember(userId = id, displayName = "Thành viên $id", role = GroupRole.MEMBER, mood = MoodType.BINHYEN),
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
            membersCsv = membersList.toCsv(),
        )

        conversationDao.upsert(entity)
        return entity.toDomain()
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
    }

    override suspend fun manageGroupMember(
        conversationId: String,
        targetUserId: String,
        newRole: GroupRole?,
        isRemove: Boolean,
    ) {
        val existing = conversationDao.getConversation(conversationId) ?: return
        val members = existing.membersCsv.toMemberList().toMutableList()

        if (isRemove) {
            members.removeAll { it.userId == targetUserId }
        } else if (newRole != null) {
            val idx = members.indexOfFirst { it.userId == targetUserId }
            if (idx >= 0) {
                members[idx] = members[idx].copy(role = newRole)
            }
        }

        conversationDao.upsert(existing.copy(membersCsv = members.toCsv()))
    }

    override suspend fun leaveGroup(conversationId: String) {
        val existing = conversationDao.getConversation(conversationId) ?: return
        val members = existing.membersCsv.toMemberList()

        // DEC-07 Check: If caller is the last remaining admin in a group with other members, block leaving
        val callerMember = members.find { it.isFromMe || it.userId == "me" }
        val otherAdmins = members.filter { (it.isFromMe || it.userId == "me").not() && it.role == GroupRole.ADMIN }

        if (callerMember?.role == GroupRole.ADMIN && otherAdmins.isEmpty() && members.size > 1) {
            throw GroupError.LastAdminCannotLeave
        }

        val remainingMembers = members.filterNot { it.isFromMe || it.userId == "me" }
        conversationDao.upsert(existing.copy(membersCsv = remainingMembers.toCsv()))
    }

    override suspend fun toggleReaction(messageId: String, mood: MoodType) {
        val message = messageDao.getMessage(messageId) ?: return
        val reactions = message.reactionsCsv.toReactionList(messageId).toMutableList()

        val myExistingIndex = reactions.indexOfFirst { it.userId == "me" }
        if (myExistingIndex >= 0) {
            val existingReaction = reactions[myExistingIndex]
            if (existingReaction.mood == mood) {
                // Remove reaction if clicking the same mood again
                reactions.removeAt(myExistingIndex)
            } else {
                // Change to new mood
                reactions[myExistingIndex] = existingReaction.copy(mood = mood)
            }
        } else {
            // Add new reaction
            reactions.add(
                MessageReaction(
                    messageId = messageId,
                    userId = "me",
                    userName = "Tôi",
                    mood = mood,
                    isFromMe = true,
                ),
            )
        }

        val newCsv = reactions.joinToString(";") { "${it.userId}|${it.userName}|${it.mood.id}" }
        messageDao.insert(message.copy(reactionsCsv = newCsv))
    }

    override suspend fun togglePinMessage(conversationId: String, messageId: String) {
        val convo = conversationDao.getConversation(conversationId) ?: return
        val message = messageDao.getMessage(messageId) ?: return

        if (convo.pinnedMessageId == messageId) {
            // Unpin
            conversationDao.upsert(convo.copy(pinnedMessageId = null, pinnedMessageSnippet = null))
            messageDao.insert(message.copy(isPinned = false))
        } else {
            // Pin
            conversationDao.upsert(
                convo.copy(
                    pinnedMessageId = messageId,
                    pinnedMessageSnippet = message.content.take(60),
                ),
            )
            messageDao.insert(message.copy(isPinned = true))
        }
    }

    override suspend fun forwardMessage(
        message: ChatMessage,
        recipientConversationIds: List<String>,
    ): List<ChatMessage> {
        val forwardedList = mutableListOf<ChatMessage>()
        val now = System.currentTimeMillis()

        recipientConversationIds.forEach { targetConvoId ->
            val newMsgId = UUID.randomUUID().toString()
            val opId = UUID.randomUUID().toString()

            val entity = MessageEntity(
                id = newMsgId,
                conversationId = targetConvoId,
                senderId = "me",
                senderName = "Tôi",
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

            val outbox = OutboxOperationEntity(
                id = opId,
                conversationId = targetConvoId,
                messageId = newMsgId,
                status = MessageStatus.PENDING.value,
                attemptCount = 0,
                createdAtEpochMs = now,
            )

            suspend fun persistForward() {
                messageDao.insert(entity)
                outboxDao.insert(outbox)
                updateConversationSummary(targetConvoId, message.content, message.mood, now)
            }

            database?.withTransaction {
                persistForward()
            } ?: persistForward()

            forwardedList.add(entity.toDomain())
        }

        return forwardedList
    }

    override suspend fun retryFailedMessage(messageId: String) {
        val message = messageDao.getMessage(messageId) ?: return
        val opId = UUID.randomUUID().toString()
        val outbox = OutboxOperationEntity(
            id = opId,
            conversationId = message.conversationId,
            messageId = messageId,
            status = MessageStatus.PENDING.value,
            attemptCount = 0,
            createdAtEpochMs = System.currentTimeMillis(),
        )

        suspend fun persistRetry() {
            messageDao.updateStatus(messageId, MessageStatus.PENDING.value)
            outboxDao.insert(outbox)
        }

        database?.withTransaction {
            persistRetry()
        } ?: persistRetry()
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
        isPinned = isPinned,
        reactions = reactionsCsv.toReactionList(id),
    )

    private fun ConversationEntity.toDomain(): Conversation = Conversation(
        id = id,
        title = title,
        mood = MoodType.fromId(mood),
        lastMessage = lastMessage ?: "",
        lastMessageTimeEpochMs = lastMessageTimeEpochMs ?: 0L,
        unreadCount = unreadCount,
        isGroup = isGroup,
        avatarUrl = avatarUrl,
        pinnedMessageId = pinnedMessageId,
        pinnedMessageSnippet = pinnedMessageSnippet,
        daysTogether = daysTogether ?: 100,
        anniversaryDate = anniversaryDate ?: "14/02/2024",
        customNickname = customNickname,
        isMuted = isMuted,
        members = membersCsv.toMemberList(),
    )

    private fun List<GroupMember>.toCsv(): String = joinToString(";") { "${it.userId}|${it.displayName}|${it.role.name}|${it.mood.id}" }

    private fun String?.toMemberList(): List<GroupMember> {
        if (this.isNullOrBlank()) return emptyList()
        return split(";").mapNotNull { part ->
            val tokens = part.split("|")
            if (tokens.size >= 4) {
                GroupMember(
                    userId = tokens[0],
                    displayName = tokens[1],
                    role = runCatching { GroupRole.valueOf(tokens[2]) }.getOrDefault(GroupRole.MEMBER),
                    mood = MoodType.fromId(tokens[3]),
                    isFromMe = tokens[0] == "me",
                )
            } else {
                null
            }
        }
    }

    private fun String?.toReactionList(msgId: String): List<MessageReaction> {
        if (this.isNullOrBlank()) return emptyList()
        return split(";").mapNotNull { part ->
            val tokens = part.split("|")
            if (tokens.size >= 3) {
                MessageReaction(
                    messageId = msgId,
                    userId = tokens[0],
                    userName = tokens[1],
                    mood = MoodType.fromId(tokens[2]),
                    isFromMe = tokens[0] == "me",
                )
            } else {
                null
            }
        }
    }
}
