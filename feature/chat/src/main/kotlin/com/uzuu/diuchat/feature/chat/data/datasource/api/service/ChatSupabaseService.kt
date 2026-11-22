package com.uzuu.diuchat.feature.chat.data.datasource.api.service

import com.uzuu.diuchat.feature.chat.data.datasource.api.model.ConversationDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.response.ChatMessagesResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.HasRecord
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecordOrNull
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import timber.log.Timber

@Suppress("TooManyFunctions")
class ChatSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    fun currentUserId(): String? = runCatching { supabaseClient?.auth?.currentUserOrNull()?.id }.getOrNull()

    fun currentUserDisplayName(): String? = runCatching {
        val meta = supabaseClient?.auth?.currentUserOrNull()?.userMetadata
        (meta?.get("full_name") as? JsonPrimitive)?.contentOrNull
            ?: (meta?.get("name") as? JsonPrimitive)?.contentOrNull
            ?: (meta?.get("display_name") as? JsonPrimitive)?.contentOrNull
    }.getOrNull()

    suspend fun fetchConversations(): Result<List<ConversationDto>> {
        val client = supabaseClient ?: return Result.success(emptyList())
        val myId = currentUserId() ?: return Result.success(emptyList())
        return runCatching {
            val conversations = client.postgrest
                .from(TABLE_CONVERSATIONS)
                .select()
                .decodeList<ConversationDto>()
            resolveDirectConversationTitles(client, myId, conversations)
        }.onFailure(::rethrowCancellation)
    }

    suspend fun fetchMessages(conversationId: String): Result<ChatMessagesResponse> {
        val client = supabaseClient ?: return Result.success(ChatMessagesResponse(conversationId = conversationId))
        return runCatching {
            val rawMessages =
                client.postgrest
                    .from(TABLE_MESSAGES)
                    .select {
                        filter {
                            eq("conversation_id", conversationId)
                        }
                        order("created_at", Order.ASCENDING)
                    }.decodeList<MessageDto>()
                    .filter { it.deletedAt == null }
            val rawConversation =
                client.postgrest
                    .from(TABLE_CONVERSATIONS)
                    .select {
                        filter {
                            eq("id", conversationId)
                        }
                    }.decodeSingleOrNull<ConversationDto>()

            val myId = currentUserId()
            val resolvedConversation = if (rawConversation != null && myId != null) {
                resolveDirectConversationTitles(client, myId, listOf(rawConversation)).firstOrNull()
            } else {
                rawConversation
            }
            val resolvedMessages = enrichSenderNames(client, rawMessages)

            ChatMessagesResponse(
                conversationId = conversationId,
                messages = resolvedMessages,
                conversation = resolvedConversation,
            )
        }.onFailure(::rethrowCancellation)
    }

    /**
     * Lắng nghe tin nhắn mới của một cuộc hội thoại qua hai nguồn trên cùng một channel:
     * - Postgres Changes (cần bật publication `supabase_realtime` cho bảng `messages`).
     * - Broadcast event [EVENT_NEW_MESSAGE] (fallback khi chưa bật replication).
     * Channel được unsubscribe/remove khi collector bị hủy.
     */
    fun observeRealtimeMessages(conversationId: String): Flow<MessageDto> {
        val client = supabaseClient ?: return emptyFlow()
        return channelFlow {
            val channel = client.channel(channelName(conversationId))
            val postgresMessages =
                channel
                    .postgresChangeFlow<PostgresAction>(schema = SCHEMA_PUBLIC) {
                        table = TABLE_MESSAGES
                        filter("conversation_id", FilterOperator.EQ, conversationId)
                    }.mapNotNull { action ->
                        (action as? HasRecord)?.let { record ->
                            runCatching { record.decodeRecordOrNull<MessageDto>() }.getOrNull()
                        }
                    }
            val broadcastMessages = channel.broadcastFlow<MessageDto>(event = EVENT_NEW_MESSAGE)

            launch {
                merge(postgresMessages, broadcastMessages)
                    .filter { it.conversationId == conversationId && it.deletedAt == null }
                    .collect { send(it) }
            }

            runCatching { channel.subscribe() }.onFailure(::rethrowCancellation)

            awaitClose {
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    removeChannelSafely(client, channel)
                }
            }
        }
    }

    suspend fun upsertMessage(message: MessageDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_MESSAGES).upsert(message)
        }.map { }
            .onFailure { error ->
                rethrowCancellation(error)
                Timber.w(error, "Failed to upsert message %s", message.id)
            }
            .onSuccess { broadcastNewMessage(client, message) }
    }

    suspend fun upsertConversation(conversation: ConversationDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_CONVERSATIONS).upsert(conversation)
        }.map { }.onFailure(::rethrowCancellation)
    }

    suspend fun insertConversationMembers(
        conversationId: String,
        memberIds: List<String>,
        creatorId: String,
    ): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            val rows = buildList {
                add(MemberInsertRow(conversationId = conversationId, userId = creatorId, role = "admin"))
                memberIds.distinct().filter { it != creatorId }.forEach { memberId ->
                    add(MemberInsertRow(conversationId = conversationId, userId = memberId, role = "member"))
                }
            }
            client.postgrest.from(TABLE_CONVERSATION_MEMBERS).upsert(rows)
        }.map { }.onFailure(::rethrowCancellation)
    }

    suspend fun uploadMedia(
        bucketPath: String,
        bytes: ByteArray,
    ): Result<String> {
        val client = supabaseClient ?: return Result.success("")
        return runCatching {
            val bucket = client.storage.from(BUCKET_CHAT_MEDIA)
            bucket.upload(path = bucketPath, data = bytes) {
                upsert = true
            }
            bucket.publicUrl(bucketPath)
        }.onFailure(::rethrowCancellation)
    }

    private suspend fun resolveDirectConversationTitles(
        client: SupabaseClient,
        myId: String,
        conversations: List<ConversationDto>,
    ): List<ConversationDto> {
        val directIds = conversations
            .filter { it.type.equals("direct", ignoreCase = true) && it.title.isNullOrBlank() }
            .map { it.id }
        if (directIds.isEmpty()) return conversations

        val members = runCatching {
            client.postgrest
                .from(TABLE_CONVERSATION_MEMBERS)
                .select {
                    filter { isIn("conversation_id", directIds) }
                }.decodeList<MemberRow>()
        }.getOrDefault(emptyList())

        val peerByConvo = members
            .groupBy { it.conversationId }
            .mapValues { (_, list) -> list.firstOrNull { it.userId != myId } }
        val peerIds = peerByConvo.values.mapNotNull { it?.userId }.distinct()
        val profiles = fetchProfilesById(client, peerIds)

        return conversations.map { convo ->
            val peer = peerByConvo[convo.id]
            val profile = peer?.userId?.let { profiles[it] }
            val resolvedTitle = peer?.customNickname?.takeIf { it.isNotBlank() }
                ?: profile?.displayName?.takeIf { it.isNotBlank() }
                ?: convo.title
            convo.copy(
                title = resolvedTitle,
                moodId = profile?.currentMood ?: convo.moodId,
                avatarUrl = convo.avatarUrl ?: profile?.avatarUrl,
            )
        }
    }

    private suspend fun enrichSenderNames(
        client: SupabaseClient,
        messages: List<MessageDto>,
    ): List<MessageDto> {
        val missingNameSenderIds = messages
            .filter { it.metadata["sender_name"]?.jsonPrimitive?.contentOrNull.isNullOrBlank() }
            .map { it.senderId }
            .distinct()
        if (missingNameSenderIds.isEmpty()) return messages

        val profiles = fetchProfilesById(client, missingNameSenderIds)
        return messages.map { msg ->
            val existingName = msg.metadata["sender_name"]?.jsonPrimitive?.contentOrNull
            val profileName = profiles[msg.senderId]?.displayName
            if (!existingName.isNullOrBlank() || profileName.isNullOrBlank()) {
                msg
            } else {
                val updatedMeta = buildJsonObject {
                    msg.metadata.forEach { (k, v) -> put(k, v) }
                    put("sender_name", profileName)
                }
                msg.copy(metadata = updatedMeta)
            }
        }
    }

    private suspend fun fetchProfilesById(
        client: SupabaseClient,
        userIds: List<String>,
    ): Map<String, ProfileRow> = if (userIds.isEmpty()) {
        emptyMap()
    } else {
        runCatching {
            client.postgrest
                .from(TABLE_USER_PROFILES)
                .select {
                    filter { isIn("id", userIds) }
                }.decodeList<ProfileRow>()
                .associateBy { it.id }
        }.getOrDefault(emptyMap())
    }

    /** Broadcast không bắt buộc: lỗi ở đây không làm hỏng ACK của upsert. */
    private suspend fun broadcastNewMessage(
        client: SupabaseClient,
        message: MessageDto,
    ) {
        runCatching {
            client
                .channel(channelName(message.conversationId))
                .broadcast(event = EVENT_NEW_MESSAGE, message = message)
        }.onFailure(::rethrowCancellation)
    }

    private suspend fun removeChannelSafely(
        client: SupabaseClient,
        channel: RealtimeChannel,
    ) {
        withContext(NonCancellable) {
            runCatching { channel.unsubscribe() }
            runCatching { client.realtime.removeChannel(channel) }
        }
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    @Serializable
    private data class MemberRow(
        @SerialName("conversation_id")
        val conversationId: String,
        @SerialName("user_id")
        val userId: String,
        @SerialName("custom_nickname")
        val customNickname: String? = null,
    )

    @Serializable
    private data class MemberInsertRow(
        @SerialName("conversation_id")
        val conversationId: String,
        @SerialName("user_id")
        val userId: String,
        @SerialName("role")
        val role: String,
    )

    @Serializable
    private data class ProfileRow(
        @SerialName("id")
        val id: String,
        @SerialName("display_name")
        val displayName: String,
        @SerialName("current_mood")
        val currentMood: String? = null,
        @SerialName("avatar_url")
        val avatarUrl: String? = null,
    )

    private companion object {
        const val TABLE_CONVERSATIONS = "conversations"
        const val TABLE_CONVERSATION_MEMBERS = "conversation_members"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TABLE_MESSAGES = "messages"
        const val BUCKET_CHAT_MEDIA = "chat-media"
        const val SCHEMA_PUBLIC = "public"
        const val EVENT_NEW_MESSAGE = "new_message"

        fun channelName(conversationId: String): String = "chat_messages_$conversationId"
    }
}
