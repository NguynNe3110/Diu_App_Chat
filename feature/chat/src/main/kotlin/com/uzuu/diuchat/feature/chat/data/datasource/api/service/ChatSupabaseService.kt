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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import timber.log.Timber
import java.time.Instant
import kotlin.time.Duration.Companion.days

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
                            eq(COL_CONVERSATION_ID, conversationId)
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
            val enrichedMessages = enrichSenderNames(client, rawMessages)
            val sealedEnriched = enrichSealedLetters(client, enrichedMessages)
            val resolvedMessages = signPrivateMediaUrls(client, sealedEnriched)

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
                        filter(COL_CONVERSATION_ID, FilterOperator.EQ, conversationId)
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
            runCatching {
                bucket.createSignedUrl(path = bucketPath, expiresIn = SIGNED_URL_EXPIRY_DAYS.days)
            }.getOrElse {
                bucket.publicUrl(bucketPath)
            }
        }.onFailure(::rethrowCancellation)
    }

    private suspend fun enrichSealedLetters(
        client: SupabaseClient,
        messages: List<MessageDto>,
    ): List<MessageDto> {
        val sealedIds = messages
            .filter { it.type.equals("sealed_letter", ignoreCase = true) }
            .map { it.id }
            .distinct()
        if (sealedIds.isEmpty()) return messages

        val unlockedLetters = runCatching {
            client.postgrest
                .from(TABLE_SEALED_LETTERS)
                .select {
                    filter { isIn(COL_MESSAGE_ID, sealedIds) }
                }.decodeList<SealedLetterContentRow>()
                .associateBy { it.messageId }
        }.getOrDefault(emptyMap())

        return messages.map { msg ->
            if (!msg.type.equals("sealed_letter", ignoreCase = true)) {
                msg
            } else {
                val letterRow = unlockedLetters[msg.id]
                if (letterRow != null) {
                    val unlockMs = runCatching { Instant.parse(letterRow.unlockAt).toEpochMilli() }.getOrNull()
                    val updatedMeta = buildJsonObject {
                        msg.metadata.forEach { (k, v) -> put(k, v) }
                        if (unlockMs != null) put("unlock_at_epoch_ms", unlockMs)
                        put("unlock_at", letterRow.unlockAt)
                    }
                    msg.copy(content = letterRow.sealedContent, metadata = updatedMeta)
                } else {
                    msg
                }
            }
        }
    }

    private suspend fun signPrivateMediaUrls(
        client: SupabaseClient,
        messages: List<MessageDto>,
    ): List<MessageDto> {
        val bucket = client.storage.from(BUCKET_CHAT_MEDIA)
        return messages.map { msg ->
            val rawUrl = msg.mediaUrl
            if (rawUrl.isNullOrBlank() || rawUrl.contains("/object/sign/")) {
                msg
            } else {
                val storagePath = extractStoragePath(rawUrl)
                if (storagePath != null) {
                    val signed =
                        runCatching {
                            bucket.createSignedUrl(path = storagePath, expiresIn = SIGNED_URL_EXPIRY_DAYS.days)
                        }.getOrNull()
                    if (!signed.isNullOrBlank()) msg.copy(mediaUrl = signed) else msg
                } else {
                    msg
                }
            }
        }
    }

    private fun extractStoragePath(url: String): String? {
        val marker = "/object/public/$BUCKET_CHAT_MEDIA/"
        val idx = url.indexOf(marker)
        return if (idx >= 0) {
            url.substring(idx + marker.length).substringBefore("?").takeIf { it.isNotBlank() }
        } else if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("content://")) {
            url.takeIf { it.isNotBlank() }
        } else {
            null
        }
    }

    private suspend fun resolveDirectConversationTitles(
        client: SupabaseClient,
        myId: String,
        conversations: List<ConversationDto>,
    ): List<ConversationDto> {
        val directIds = conversations
            .filter { it.type.equals("direct", ignoreCase = true) }
            .map { it.id }
        if (directIds.isEmpty()) return conversations

        val members = runCatching {
            client.postgrest
                .from(TABLE_CONVERSATION_MEMBERS)
                .select {
                    filter { isIn(COL_CONVERSATION_ID, directIds) }
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
            val safeAvatar = (convo.avatarUrl ?: profile?.avatarUrl)?.takeUnless { url ->
                url.contains("googleusercontent.com", ignoreCase = true)
            }
            convo.copy(
                title = resolvedTitle,
                moodId = profile?.currentMood ?: convo.moodId,
                avatarUrl = safeAvatar,
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
    fun observeProfileUpdates(): Flow<Unit> {
        val client = supabaseClient ?: return emptyFlow()
        return channelFlow {
            val dbChannel = client.channel(CHANNEL_CHAT_PROFILE_UPDATES)
            val profileChanges =
                dbChannel
                    .postgresChangeFlow<PostgresAction>(schema = SCHEMA_PUBLIC) {
                        table = TABLE_USER_PROFILES
                    }.map { }
            val globalChannel =
                client.realtime.subscriptions["realtime:$CHANNEL_GLOBAL_SYNC"]
                    ?: client.channel(CHANNEL_GLOBAL_SYNC)
            val profileBroadcasts =
                globalChannel.broadcastFlow<JsonObject>(event = EVENT_PROFILE_UPDATED).map { }

            launch {
                merge(profileChanges, profileBroadcasts).collect { send(Unit) }
            }

            runCatching { dbChannel.subscribe() }.onFailure(::rethrowCancellation)
            if (globalChannel.status.value != RealtimeChannel.Status.SUBSCRIBED) {
                runCatching { globalChannel.subscribe() }.onFailure(::rethrowCancellation)
            }

            awaitClose {
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    removeChannelSafely(client, dbChannel)
                }
            }
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            runCatching {
                client.postgrest
                    .from(TABLE_SEALED_LETTERS)
                    .delete {
                        filter { eq(COL_MESSAGE_ID, messageId) }
                    }
            }
            client.postgrest
                .from(TABLE_MESSAGES)
                .update(
                    buildJsonObject {
                        put("deleted_at", Instant.now().toString())
                    },
                ) {
                    filter {
                        eq("id", messageId)
                    }
                }
        }.map { }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to soft-delete message %s", messageId)
        }
    }

    suspend fun toggleStarMessage(messageId: String): Result<Boolean> {
        val client = supabaseClient ?: return Result.success(true)
        val myId = currentUserId() ?: return Result.success(true)
        return runCatching {
            val existing =
                client.postgrest
                    .from(TABLE_STARRED_MESSAGES)
                    .select {
                        filter {
                            eq(COL_USER_ID, myId)
                            eq(COL_MESSAGE_ID, messageId)
                        }
                    }.decodeList<JsonObject>()
            if (existing.isNotEmpty()) {
                client.postgrest.from(TABLE_STARRED_MESSAGES).delete {
                    filter {
                        eq(COL_USER_ID, myId)
                        eq(COL_MESSAGE_ID, messageId)
                    }
                }
                false
            } else {
                client.postgrest.from(TABLE_STARRED_MESSAGES).insert(
                    buildJsonObject {
                        put(COL_USER_ID, myId)
                        put(COL_MESSAGE_ID, messageId)
                    },
                )
                true
            }
        }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to toggle starred message %s", messageId)
        }
    }

    suspend fun deleteConversation(conversationId: String): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        val myId = currentUserId() ?: return Result.success(Unit)
        return runCatching {
            client.postgrest
                .from(TABLE_CONVERSATION_MEMBERS)
                .update(
                    buildJsonObject {
                        put("left_at", Instant.now().toString())
                    },
                ) {
                    filter {
                        eq(COL_CONVERSATION_ID, conversationId)
                        eq(COL_USER_ID, myId)
                    }
                }
            runCatching {
                val globalChannel =
                    client.realtime.subscriptions["realtime:$CHANNEL_GLOBAL_SYNC"]
                        ?: client.channel(CHANNEL_GLOBAL_SYNC)
                if (globalChannel.status.value != RealtimeChannel.Status.SUBSCRIBED) {
                    runCatching { globalChannel.subscribe() }
                }
                globalChannel.broadcast(
                    event = EVENT_CONVERSATION_DELETED,
                    message =
                    buildJsonObject {
                        put(COL_CONVERSATION_ID, conversationId)
                        put(COL_USER_ID, myId)
                    },
                )
            }.onFailure(::rethrowCancellation)
        }.map { }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to delete conversation %s", conversationId)
        }
    }

    suspend fun insertSealedLetterForMessage(
        messageId: String,
        conversationId: String,
        content: String,
        unlockAtEpochMs: Long,
    ): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        val myId = currentUserId() ?: return Result.success(Unit)
        return runCatching {
            val members =
                client.postgrest
                    .from(TABLE_CONVERSATION_MEMBERS)
                    .select {
                        filter { eq(COL_CONVERSATION_ID, conversationId) }
                    }.decodeList<MemberRow>()
            val recipientId = members.firstOrNull { it.userId != myId }?.userId
            if (recipientId != null) {
                client.postgrest.from(TABLE_SEALED_LETTERS).insert(
                    buildJsonObject {
                        put(COL_MESSAGE_ID, messageId)
                        put("sender_id", myId)
                        put("recipient_id", recipientId)
                        put("unlock_at", Instant.ofEpochMilli(unlockAtEpochMs).toString())
                        put("sealed_content", content)
                        put("is_opened", false)
                    },
                )
            }
        }.map { }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to insert sealed_letter for message %s", messageId)
        }
    }

    suspend fun markConversationRead(conversationId: String) {
        val client = supabaseClient ?: return
        val myId = currentUserId() ?: return
        runCatching {
            client.postgrest
                .from(TABLE_CONVERSATION_MEMBERS)
                .update(
                    buildJsonObject {
                        put("last_read_at", Instant.now().toString())
                    },
                ) {
                    filter {
                        eq(COL_CONVERSATION_ID, conversationId)
                        eq(COL_USER_ID, myId)
                    }
                }
        }.onFailure(::rethrowCancellation)
        runCatching {
            val globalChannel =
                client.realtime.subscriptions["realtime:$CHANNEL_GLOBAL_SYNC"]
                    ?: client.channel(CHANNEL_GLOBAL_SYNC)
            if (globalChannel.status.value != RealtimeChannel.Status.SUBSCRIBED) {
                runCatching { globalChannel.subscribe() }
            }
            globalChannel.broadcast(
                event = EVENT_CONVERSATION_READ,
                message =
                buildJsonObject {
                    put(COL_CONVERSATION_ID, conversationId)
                    put(COL_USER_ID, myId)
                },
            )
        }.onFailure(::rethrowCancellation)
    }

    suspend fun broadcastTyping(
        conversationId: String,
        isTyping: Boolean,
    ) {
        val client = supabaseClient ?: return
        val myId = currentUserId() ?: return
        runCatching {
            val topic = typingChannelName(conversationId)
            val ch =
                client.realtime.subscriptions["realtime:$topic"]
                    ?: client.channel(topic)
            if (ch.status.value != RealtimeChannel.Status.SUBSCRIBED) {
                runCatching { ch.subscribe() }
            }
            ch.broadcast(
                event = EVENT_TYPING,
                message =
                buildJsonObject {
                    put("user_id", myId)
                    put("is_typing", isTyping)
                },
            )
        }.onFailure(::rethrowCancellation)
    }

    fun observeTypingStatus(conversationId: String): Flow<Boolean> {
        val client = supabaseClient ?: return emptyFlow()
        val myId = currentUserId()
        return channelFlow {
            val topic = typingChannelName(conversationId)
            val ch =
                client.realtime.subscriptions["realtime:$topic"]
                    ?: client.channel(topic)
            val typingFlow =
                ch.broadcastFlow<JsonObject>(event = EVENT_TYPING).mapNotNull { payload ->
                    val senderId = payload["user_id"]?.jsonPrimitive?.contentOrNull
                    if (senderId != null && senderId == myId) {
                        null
                    } else {
                        payload["is_typing"]?.jsonPrimitive?.booleanOrNull
                    }
                }
            launch {
                typingFlow.collect { send(it) }
            }
            if (ch.status.value != RealtimeChannel.Status.SUBSCRIBED) {
                runCatching { ch.subscribe() }.onFailure(::rethrowCancellation)
            }
            awaitClose {
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    removeChannelSafely(client, ch)
                }
            }
        }
    }

    private suspend fun broadcastNewMessage(
        client: SupabaseClient,
        message: MessageDto,
    ) {
        runCatching {
            val ch =
                client.realtime.subscriptions["realtime:${channelName(message.conversationId)}"]
                    ?: client.channel(channelName(message.conversationId))
            ch.broadcast(event = EVENT_NEW_MESSAGE, message = message)
        }.onFailure(::rethrowCancellation)
        runCatching {
            val globalChannel =
                client.realtime.subscriptions["realtime:$CHANNEL_GLOBAL_SYNC"]
                    ?: client.channel(CHANNEL_GLOBAL_SYNC)
            globalChannel.broadcast(
                event = EVENT_NEW_MESSAGE,
                message =
                buildJsonObject {
                    put("conversation_id", message.conversationId)
                    put("message_id", message.id)
                },
            )
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
    private data class SealedLetterContentRow(
        @SerialName("message_id")
        val messageId: String,
        @SerialName("unlock_at")
        val unlockAt: String,
        @SerialName("sealed_content")
        val sealedContent: String,
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
        const val COL_CONVERSATION_ID = "conversation_id"
        const val COL_USER_ID = "user_id"
        const val COL_MESSAGE_ID = "message_id"
        const val TABLE_CONVERSATIONS = "conversations"
        const val TABLE_CONVERSATION_MEMBERS = "conversation_members"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TABLE_MESSAGES = "messages"
        const val TABLE_STARRED_MESSAGES = "starred_messages"
        const val TABLE_SEALED_LETTERS = "sealed_letters"
        const val BUCKET_CHAT_MEDIA = "chat-media"
        const val SCHEMA_PUBLIC = "public"
        const val CHANNEL_GLOBAL_SYNC = "diu_global_sync"
        const val CHANNEL_CHAT_PROFILE_UPDATES = "chat_profile_updates"
        const val EVENT_NEW_MESSAGE = "new_message"
        const val EVENT_PROFILE_UPDATED = "profile_updated"
        const val EVENT_CONVERSATION_READ = "conversation_read"
        const val EVENT_CONVERSATION_DELETED = "conversation_deleted"
        const val EVENT_TYPING = "typing"
        const val SIGNED_URL_EXPIRY_DAYS = 30

        fun channelName(conversationId: String): String = "chat_messages_$conversationId"

        fun typingChannelName(conversationId: String): String = "chat_typing_$conversationId"
    }
}
