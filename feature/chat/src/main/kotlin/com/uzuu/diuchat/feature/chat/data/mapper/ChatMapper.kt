package com.uzuu.diuchat.feature.chat.data.mapper

import com.uzuu.diuchat.feature.chat.data.datasource.api.model.ConversationDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.ConversationEntity
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.MessageEntity
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.GroupMember
import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageReaction
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType

class ChatMapper {
    fun messageEntityToDomain(entity: MessageEntity): ChatMessage = ChatMessage(
        id = entity.id,
        conversationId = entity.conversationId,
        senderId = entity.senderId,
        senderName = entity.senderName,
        content = entity.content,
        mood = MoodType.fromId(entity.mood),
        status = MessageStatus.fromValue(entity.status),
        replyToId = entity.replyToId,
        replyQuote = entity.replyQuote,
        createdAtEpochMs = entity.createdAtEpochMs,
        isFromMe = entity.isFromMe,
        mediaType = runCatching { MessageMediaType.valueOf(entity.mediaType) }.getOrDefault(MessageMediaType.TEXT),
        mediaUri = entity.mediaUri,
        audioDurationMs = entity.audioDurationMs,
        audioWaveform = parseWaveform(entity.audioWaveformCsv),
        caption = entity.caption,
        isPinned = entity.isPinned,
        reactions = parseReactions(entity.reactionsCsv, entity.id),
    )

    fun messageDomainToEntity(domain: ChatMessage): MessageEntity = MessageEntity(
        id = domain.id,
        conversationId = domain.conversationId,
        senderId = domain.senderId,
        senderName = domain.senderName,
        content = domain.content,
        mood = domain.mood.id,
        status = domain.status.value,
        replyToId = domain.replyToId,
        replyQuote = domain.replyQuote,
        createdAtEpochMs = domain.createdAtEpochMs,
        isFromMe = domain.isFromMe,
        mediaType = domain.mediaType.name,
        mediaUri = domain.mediaUri,
        audioDurationMs = domain.audioDurationMs,
        audioWaveformCsv = domain.audioWaveform.joinToString(","),
        caption = domain.caption,
        isPinned = domain.isPinned,
        reactionsCsv = serializeReactions(domain.reactions),
    )

    fun messageDtoToEntity(dto: MessageDto): MessageEntity = MessageEntity(
        id = dto.id,
        conversationId = dto.conversationId,
        senderId = dto.senderId,
        senderName = dto.senderName,
        content = dto.content,
        mood = dto.mood,
        status = dto.status,
        replyToId = dto.replyToId,
        replyQuote = dto.replyQuote,
        createdAtEpochMs = dto.createdAtEpochMs,
        isFromMe = dto.isFromMe,
        mediaType = dto.mediaType,
        mediaUri = dto.mediaUri,
        audioDurationMs = dto.audioDurationMs,
        audioWaveformCsv = dto.audioWaveformCsv,
        caption = dto.caption,
        isPinned = dto.isPinned,
        reactionsCsv = dto.reactionsCsv,
    )

    fun messageEntityToDto(entity: MessageEntity): MessageDto = MessageDto(
        id = entity.id,
        conversationId = entity.conversationId,
        senderId = entity.senderId,
        senderName = entity.senderName,
        content = entity.content,
        mood = entity.mood,
        status = entity.status,
        replyToId = entity.replyToId,
        replyQuote = entity.replyQuote,
        createdAtEpochMs = entity.createdAtEpochMs,
        isFromMe = entity.isFromMe,
        mediaType = entity.mediaType,
        mediaUri = entity.mediaUri,
        audioDurationMs = entity.audioDurationMs,
        audioWaveformCsv = entity.audioWaveformCsv,
        caption = entity.caption,
        isPinned = entity.isPinned,
        reactionsCsv = entity.reactionsCsv,
    )

    fun messageDtoToDomain(dto: MessageDto): ChatMessage = messageEntityToDomain(messageDtoToEntity(dto))

    fun conversationEntityToDomain(entity: ConversationEntity): Conversation = Conversation(
        id = entity.id,
        title = entity.title,
        mood = MoodType.fromId(entity.mood),
        lastMessage = entity.lastMessage ?: "",
        lastMessageTimeEpochMs = entity.lastMessageTimeEpochMs ?: 0L,
        unreadCount = entity.unreadCount,
        isGroup = entity.isGroup,
        avatarUrl = entity.avatarUrl,
        pinnedMessageId = entity.pinnedMessageId,
        pinnedMessageSnippet = entity.pinnedMessageSnippet,
        daysTogether = entity.daysTogether ?: DEFAULT_DAYS_TOGETHER,
        anniversaryDate = entity.anniversaryDate ?: DEFAULT_ANNIVERSARY_DATE,
        customNickname = entity.customNickname,
        isMuted = entity.isMuted,
        members = parseMembers(entity.membersCsv),
    )

    fun conversationDomainToEntity(domain: Conversation): ConversationEntity = ConversationEntity(
        id = domain.id,
        title = domain.title,
        mood = domain.mood.id,
        lastMessage = domain.lastMessage,
        lastMessageTimeEpochMs = domain.lastMessageTimeEpochMs,
        unreadCount = domain.unreadCount,
        isGroup = domain.isGroup,
        avatarUrl = domain.avatarUrl,
        pinnedMessageId = domain.pinnedMessageId,
        pinnedMessageSnippet = domain.pinnedMessageSnippet,
        daysTogether = domain.daysTogether,
        anniversaryDate = domain.anniversaryDate,
        customNickname = domain.customNickname,
        isMuted = domain.isMuted,
        membersCsv = serializeMembers(domain.members),
    )

    fun conversationDtoToEntity(dto: ConversationDto): ConversationEntity = ConversationEntity(
        id = dto.id,
        title = dto.title,
        mood = dto.mood,
        lastMessage = dto.lastMessage,
        lastMessageTimeEpochMs = dto.lastMessageTimeEpochMs,
        unreadCount = dto.unreadCount,
        isGroup = dto.isGroup,
        avatarUrl = dto.avatarUrl,
        pinnedMessageId = dto.pinnedMessageId,
        pinnedMessageSnippet = dto.pinnedMessageSnippet,
        daysTogether = dto.daysTogether,
        anniversaryDate = dto.anniversaryDate,
        customNickname = dto.customNickname,
        isMuted = dto.isMuted,
        membersCsv = dto.membersCsv,
    )

    fun conversationEntityToDto(entity: ConversationEntity): ConversationDto = ConversationDto(
        id = entity.id,
        title = entity.title,
        mood = entity.mood,
        lastMessage = entity.lastMessage,
        lastMessageTimeEpochMs = entity.lastMessageTimeEpochMs,
        unreadCount = entity.unreadCount,
        isGroup = entity.isGroup,
        avatarUrl = entity.avatarUrl,
        pinnedMessageId = entity.pinnedMessageId,
        pinnedMessageSnippet = entity.pinnedMessageSnippet,
        daysTogether = entity.daysTogether,
        anniversaryDate = entity.anniversaryDate,
        customNickname = entity.customNickname,
        isMuted = entity.isMuted,
        membersCsv = entity.membersCsv,
    )

    fun conversationDtoToDomain(dto: ConversationDto): Conversation = conversationEntityToDomain(conversationDtoToEntity(dto))

    fun serializeMembers(members: List<GroupMember>): String = members.joinToString(";") {
        "${it.userId}|${it.displayName}|${it.role.name}|${it.mood.id}"
    }

    fun parseMembers(membersCsv: String?): List<GroupMember> {
        if (membersCsv.isNullOrBlank()) return emptyList()
        return membersCsv.split(";").mapNotNull { part ->
            val tokens = part.split("|")
            if (tokens.size >= MEMBER_TOKEN_COUNT) {
                GroupMember(
                    userId = tokens[0],
                    displayName = tokens[1],
                    role = runCatching { GroupRole.valueOf(tokens[2]) }.getOrDefault(GroupRole.MEMBER),
                    mood = MoodType.fromId(tokens[3]),
                    isFromMe = tokens[0] == ME_USER_ID,
                )
            } else {
                null
            }
        }
    }

    fun serializeReactions(reactions: List<MessageReaction>): String = reactions.joinToString(";") {
        "${it.userId}|${it.userName}|${it.mood.id}"
    }

    fun parseReactions(
        reactionsCsv: String?,
        messageId: String,
    ): List<MessageReaction> {
        if (reactionsCsv.isNullOrBlank()) return emptyList()
        return reactionsCsv.split(";").mapNotNull { part ->
            val tokens = part.split("|")
            if (tokens.size >= REACTION_TOKEN_COUNT) {
                MessageReaction(
                    messageId = messageId,
                    userId = tokens[0],
                    userName = tokens[1],
                    mood = MoodType.fromId(tokens[2]),
                    isFromMe = tokens[0] == ME_USER_ID,
                )
            } else {
                null
            }
        }
    }

    private fun parseWaveform(waveformCsv: String?): List<Float> {
        if (waveformCsv.isNullOrBlank()) return emptyList()
        return waveformCsv.split(",").mapNotNull { it.toFloatOrNull() }
    }

    private companion object {
        const val ME_USER_ID = "me"
        const val MEMBER_TOKEN_COUNT = 4
        const val REACTION_TOKEN_COUNT = 3
        const val DEFAULT_DAYS_TOGETHER = 100
        const val DEFAULT_ANNIVERSARY_DATE = "14/02/2024"
    }
}
