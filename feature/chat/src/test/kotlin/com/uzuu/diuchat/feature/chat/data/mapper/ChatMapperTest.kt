package com.uzuu.diuchat.feature.chat.data.mapper

import com.uzuu.diuchat.feature.chat.data.datasource.api.model.ConversationDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation
import com.uzuu.diuchat.feature.chat.domain.model.GroupMember
import com.uzuu.diuchat.feature.chat.domain.model.GroupRole
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageReaction
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.time.Instant

class ChatMapperTest {
    private val sut = ChatMapper()

    @Test
    fun `message domain to entity and back preserves fields including waveform and reactions`() {
        val original = ChatMessage(
            id = "m1",
            conversationId = "c1",
            senderId = "me",
            senderName = "Tôi",
            content = "Nghe thử nha",
            mood = MoodType.THUONG,
            status = MessageStatus.SENT,
            replyToId = "m0",
            replyQuote = "Tin trước",
            createdAtEpochMs = 1_700_000_000_000L,
            isFromMe = true,
            mediaType = MessageMediaType.VOICE,
            mediaUri = "file:///voice.m4a",
            audioDurationMs = 12_000L,
            audioWaveform = listOf(0.2f, 0.8f, 0.5f),
            caption = "Ghi âm",
            isPinned = true,
            reactions = listOf(
                MessageReaction(
                    messageId = "m1",
                    userId = "me",
                    userName = "Tôi",
                    mood = MoodType.VUI,
                    isFromMe = true,
                ),
            ),
        )

        val entity = sut.messageDomainToEntity(original)
        val roundTripped = sut.messageEntityToDomain(entity)

        roundTripped shouldBeEqualTo original
    }

    @Test
    fun `message dto maps to entity and domain accurately`() {
        val createdAtIso = Instant.ofEpochMilli(1_700_000_100_000L).toString()
        val dto = MessageDto(
            id = "m2",
            conversationId = "c1",
            senderId = "partner",
            type = "text",
            content = "Chào buổi tối",
            moodId = MoodType.BINHYEN.id,
            metadata = buildJsonObject {
                put("sender_name", "Minh")
            },
            createdAt = createdAtIso,
        )

        val entity = sut.messageDtoToEntity(dto, currentUserId = "me")
        val domain = sut.messageDtoToDomain(dto, currentUserId = "me")

        sut.messageEntityToDto(entity) shouldBeEqualTo dto
        domain.id shouldBeEqualTo "m2"
        domain.mood shouldBeEqualTo MoodType.BINHYEN
        domain.senderName shouldBeEqualTo "Minh"
        domain.isFromMe shouldBeEqualTo false
    }

    @Test
    fun `conversation domain to entity and back preserves members`() {
        val original = Conversation(
            id = "g1",
            title = "Nhà mình",
            mood = MoodType.VUI,
            lastMessage = "Cuối tuần ăn lẩu nha",
            lastMessageTimeEpochMs = 1_700_000_200_000L,
            unreadCount = 2,
            isGroup = true,
            avatarUrl = "https://example.com/a.jpg",
            pinnedMessageId = "m1",
            pinnedMessageSnippet = "Nhớ mang áo ấm",
            daysTogether = 365,
            anniversaryDate = "01/01/2025",
            customNickname = "Gia đình nhỏ",
            isMuted = true,
            members = listOf(
                GroupMember(
                    userId = "me",
                    displayName = "Tôi",
                    role = GroupRole.ADMIN,
                    mood = MoodType.VUI,
                    isFromMe = true,
                ),
                GroupMember(
                    userId = "u2",
                    displayName = "Mẹ",
                    role = GroupRole.MEMBER,
                    mood = MoodType.THUONG,
                    isFromMe = false,
                ),
            ),
        )

        val entity = sut.conversationDomainToEntity(original)
        val roundTripped = sut.conversationEntityToDomain(entity)

        roundTripped shouldBeEqualTo original
    }

    @Test
    fun `conversation dto maps to entity and domain accurately`() {
        val updatedIso = Instant.ofEpochMilli(1_700_000_300_000L).toString()
        val dto = ConversationDto(
            id = "c2",
            type = "direct",
            title = "Thảo",
            moodId = MoodType.NHO.id,
            updatedAt = updatedIso,
        )

        val entity = sut.conversationDtoToEntity(dto)
        val domain = sut.conversationDtoToDomain(dto)

        sut.conversationEntityToDto(entity) shouldBeEqualTo dto
        domain.id shouldBeEqualTo "c2"
        domain.mood shouldBeEqualTo MoodType.NHO
        domain.isGroup shouldBeEqualTo false
    }
}
