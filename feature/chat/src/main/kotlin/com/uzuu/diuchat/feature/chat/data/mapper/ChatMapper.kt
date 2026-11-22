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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.time.Instant

class ChatMapper {
    fun messageEntityToDomain(entity: MessageEntity): ChatMessage {
        val mediaType = runCatching { MessageMediaType.valueOf(entity.mediaType) }.getOrDefault(MessageMediaType.TEXT)
        val isSealed = mediaType == MessageMediaType.SEALED_LETTER
        return ChatMessage(
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
            mediaType = mediaType,
            mediaUri = entity.mediaUri,
            audioDurationMs = if (isSealed) null else entity.audioDurationMs,
            audioWaveform = parseWaveform(entity.audioWaveformCsv),
            caption = entity.caption,
            isPinned = entity.isPinned,
            reactions = parseReactions(entity.reactionsCsv, entity.id),
            scheduledForLabel = if (isSealed) entity.caption else null,
            unlockAtEpochMs = if (isSealed) entity.audioDurationMs else null,
        )
    }

    fun messageDomainToEntity(domain: ChatMessage): MessageEntity {
        val isSealed = domain.mediaType == MessageMediaType.SEALED_LETTER
        return MessageEntity(
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
            audioDurationMs = if (isSealed) domain.unlockAtEpochMs else domain.audioDurationMs,
            audioWaveformCsv = domain.audioWaveform.joinToString(","),
            caption = if (isSealed) (domain.scheduledForLabel ?: domain.caption) else domain.caption,
            isPinned = domain.isPinned,
            reactionsCsv = serializeReactions(domain.reactions),
        )
    }

    fun messageDtoToEntity(
        dto: MessageDto,
        currentUserId: String? = null,
    ): MessageEntity {
        val meta = dto.metadata
        val senderName = meta["sender_name"]?.jsonPrimitive?.contentOrNull ?: "Bạn"
        val replyQuote = meta["reply_quote"]?.jsonPrimitive?.contentOrNull
        val audioWaveformCsv = meta["audio_waveform_csv"]?.jsonPrimitive?.contentOrNull ?: ""
        val reactionsCsv = meta["reactions_csv"]?.jsonPrimitive?.contentOrNull ?: ""
        val createdAtMs = parseIsoEpochMs(dto.createdAt) ?: System.currentTimeMillis()
        val mediaType = parseDtoMediaType(dto.type)
        val isSealed = mediaType == MessageMediaType.SEALED_LETTER.name
        val audioDurationMs = if (isSealed) {
            parseUnlockAtEpochMs(meta)
        } else {
            meta["audio_duration_ms"]?.jsonPrimitive?.longOrNull
        }
        val caption = if (isSealed) {
            meta["scheduled_label"]?.jsonPrimitive?.contentOrNull
                ?: meta["caption"]?.jsonPrimitive?.contentOrNull
        } else {
            meta["caption"]?.jsonPrimitive?.contentOrNull
        }
        return MessageEntity(
            id = dto.id,
            conversationId = dto.conversationId,
            senderId = dto.senderId,
            senderName = senderName,
            content = dto.content ?: "",
            mood = normalizeMoodId(dto.moodId),
            status = MessageStatus.SENT.value,
            replyToId = dto.replyToId,
            replyQuote = replyQuote,
            createdAtEpochMs = createdAtMs,
            isFromMe = currentUserId != null && dto.senderId == currentUserId,
            mediaType = mediaType,
            mediaUri = dto.mediaUrl,
            audioDurationMs = audioDurationMs,
            audioWaveformCsv = audioWaveformCsv,
            caption = caption,
            isPinned = dto.isPinned,
            reactionsCsv = reactionsCsv,
        )
    }

    fun messageEntityToDto(entity: MessageEntity): MessageDto {
        val dbType = mapEntityMediaTypeToDbType(entity.mediaType)
        val isSealed = dbType == "sealed_letter"
        val meta = buildMessageMetadata(entity, isSealed)
        val remoteContent = if (isSealed) {
            "✉️ Thư hẹn · mở vào ${entity.caption ?: "đúng giờ"}"
        } else {
            entity.content
        }
        return MessageDto(
            id = entity.id,
            conversationId = entity.conversationId,
            senderId = entity.senderId,
            replyToId = entity.replyToId,
            type = dbType,
            content = remoteContent,
            mediaUrl = entity.mediaUri,
            moodId = normalizeMoodId(entity.mood),
            metadata = meta,
            isPinned = entity.isPinned,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMs).toString(),
        )
    }

    private fun parseDtoMediaType(rawType: String): String = when (rawType.lowercase()) {
        "photo" -> MessageMediaType.PHOTO.name
        "voice" -> MessageMediaType.VOICE.name
        "location" -> MessageMediaType.LOCATION.name
        "sealed_letter" -> MessageMediaType.SEALED_LETTER.name
        else -> MessageMediaType.TEXT.name
    }

    private fun mapEntityMediaTypeToDbType(mediaType: String): String = when (mediaType.uppercase()) {
        MessageMediaType.PHOTO.name -> "photo"
        MessageMediaType.VOICE.name -> "voice"
        MessageMediaType.LOCATION.name -> "location"
        MessageMediaType.SEALED_LETTER.name -> "sealed_letter"
        else -> "text"
    }

    private fun parseIsoEpochMs(iso: String?): Long? = iso?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
    }

    private fun parseUnlockAtEpochMs(meta: kotlinx.serialization.json.JsonObject): Long? {
        val epochMs = meta["unlock_at_epoch_ms"]?.jsonPrimitive?.longOrNull
        return epochMs ?: parseIsoEpochMs(meta["unlock_at"]?.jsonPrimitive?.contentOrNull)
    }

    private fun buildMessageMetadata(
        entity: MessageEntity,
        isSealed: Boolean,
    ) = buildJsonObject {
        put("sender_name", entity.senderName)
        entity.replyQuote?.let { put("reply_quote", it) }
        if (isSealed) {
            entity.caption?.let { put("scheduled_label", it) }
            entity.audioDurationMs?.let { unlockMs ->
                put("unlock_at_epoch_ms", unlockMs)
                put("unlock_at", Instant.ofEpochMilli(unlockMs).toString())
            }
        } else {
            entity.audioDurationMs?.let { put("audio_duration_ms", it) }
            entity.caption?.let { put("caption", it) }
        }
        entity.audioWaveformCsv?.takeIf { it.isNotBlank() }?.let { put("audio_waveform_csv", it) }
        entity.reactionsCsv?.takeIf { it.isNotBlank() }?.let { put("reactions_csv", it) }
    }

    fun messageDtoToDomain(
        dto: MessageDto,
        currentUserId: String? = null,
    ): ChatMessage = messageEntityToDomain(messageDtoToEntity(dto, currentUserId))

    private fun normalizeMoodId(raw: String?): String = when (raw?.lowercase()) {
        "thuong", "vui", "binhyen", "nho", "tram" -> raw.lowercase()
        else -> "thuong"
    }

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

    fun conversationDtoToEntity(dto: ConversationDto): ConversationEntity {
        val updatedAtMs = dto.updatedAt?.let {
            runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
        }
        return ConversationEntity(
            id = dto.id,
            title = dto.title ?: "Cuộc trò chuyện",
            mood = normalizeMoodId(dto.moodId),
            lastMessage = null,
            lastMessageTimeEpochMs = updatedAtMs,
            unreadCount = 0,
            isGroup = dto.type.equals("group", ignoreCase = true),
            avatarUrl = dto.avatarUrl,
        )
    }

    fun conversationEntityToDto(entity: ConversationEntity): ConversationDto = ConversationDto(
        id = entity.id,
        type = if (entity.isGroup) "group" else "direct",
        title = entity.title,
        avatarUrl = entity.avatarUrl,
        moodId = normalizeMoodId(entity.mood),
        updatedAt = entity.lastMessageTimeEpochMs?.let { Instant.ofEpochMilli(it).toString() },
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
